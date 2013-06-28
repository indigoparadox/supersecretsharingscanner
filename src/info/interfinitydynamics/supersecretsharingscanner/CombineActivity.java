package info.interfinitydynamics.supersecretsharingscanner;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.tiemens.secretshare.exceptions.SecretShareException;
import com.tiemens.secretshare.main.cli.MainCombine.CombineOutput;
import com.tiemens.secretshare.main.cli.MainCombine.CombineInput;

import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

@SuppressLint("DefaultLocale")
public class CombineActivity extends Activity {

   class ShareData {
      protected int cIntShareNum;
      protected String cStrShareString;

      public ShareData( int intShareNumIn, String strShareStringIn ) {
         cIntShareNum = intShareNumIn;
         cStrShareString = strShareStringIn;
      }

      public String toString() {
         return String.format( "%d - %s", cIntShareNum, cStrShareString );
      }
   }

   protected ArrayList<ShareData> caShrShareList = new ArrayList<ShareData>();

   @Override
   protected void onCreate( Bundle savedInstanceState ) {
      super.onCreate( savedInstanceState );
      setContentView( R.layout.activity_combine );
      
      // Ignore input to the result text box.
      final EditText txtResult = (EditText)findViewById( R.id.txtResult );
      txtResult.setKeyListener( null );

      final Button btnAddShare = (Button)findViewById( R.id.btnAddShare );
      btnAddShare.setOnClickListener( new View.OnClickListener() {
         public void onClick( View v ) {

            String strShareString;
            final EditText txtShareNum =
               (EditText)findViewById( R.id.txtShareNum );
            final EditText txtShareString =
               (EditText)findViewById( R.id.txtShareString );
            
            try {
               Pattern ptnShareString = 
                  Pattern.compile( "(bigintcs:[A-Za-z0-9\\-]+)" );
               Matcher mtcShareString = 
                  ptnShareString.matcher( txtShareString.getText().toString() );
               if( mtcShareString.find() ) {
                  strShareString = mtcShareString.group( 1 );
               } else {
                  // TODO: Use a resource string.
                  throw new SecretShareException( "Invalid share format." );
               }
               
               addShare( 
                  Integer.parseInt( txtShareNum.getText().toString() ),
                  strShareString
               );
            } catch( SecretShareException ex ) {
               Toast.makeText(
                  CombineActivity.this, ex.getMessage(), Toast.LENGTH_LONG
               ).show();
               return;
            } catch( NumberFormatException ex ) {
               Toast.makeText(
                  CombineActivity.this,
                  // TODO: Use a resource string.
                  "Invalid share number format.",
                  Toast.LENGTH_LONG
               ).show();
               return;
            }

            txtShareNum.setText( "" );
            txtShareString.setText( "" );
         }
      } );

      final Button btnScan = (Button)findViewById( R.id.btnScan );
      btnScan.setOnClickListener( new View.OnClickListener() {

         @Override
         public void onClick( View v ) {
            IntentIntegrator itiScan = new IntentIntegrator( CombineActivity.this );
            itiScan.initiateScan();
         }
      } );

      final Button btnCombine = (Button)findViewById( R.id.btnCombine );
      btnCombine.setOnClickListener( new View.OnClickListener() {
         public void onClick( View v ) {
            final EditText txtResult = (EditText)findViewById( R.id.txtResult );
            String strResult = combineTAShares( caShrShareList );
            if( null != strResult ) {
               txtResult.setText( strResult );
            }
         }
      } );
      
      final ListView lstShares = (ListView)findViewById( R.id.lstShares );
      lstShares.setOnItemClickListener( new OnItemClickListener() {
         @Override
         public void onItemClick(
            AdapterView<?> advParent,
            View viwView,
            int intPosition,
            long lngID
         ) {
            final ListView lstShares = (ListView)findViewById( R.id.lstShares );
            final int intSelectedItem = intPosition;
            
            // Ask to delete selected item.
            DialogInterface.OnClickListener lisDialogClickListener = 
               new DialogInterface.OnClickListener() {
               
               @SuppressWarnings("unchecked")
               @Override
               public void onClick( DialogInterface dliDialog, int intWhich ) {
                   switch( intWhich ){
                   case DialogInterface.BUTTON_POSITIVE:
                      //lstShares.
                      
                      caShrShareList.remove( intSelectedItem );
                      
                      ((ArrayAdapter<ShareData>)lstShares
                         .getAdapter()).notifyDataSetChanged();

                      break;
                   }
               }
           };

           AlertDialog.Builder adbBuilder = 
              new AlertDialog.Builder( CombineActivity.this );
           // TODO: Use a resource string.
           adbBuilder.setMessage( "Delete this share?" )
              .setPositiveButton( "Yes", lisDialogClickListener )
              .setNegativeButton( "No", lisDialogClickListener )
              .show();
         }
      } );
      
      // Populate the share listbox.
      ArrayAdapter<ShareData> adpShareAdapter =
         new ArrayAdapter<CombineActivity.ShareData>( getApplicationContext(),
            android.R.layout.simple_list_item_1, caShrShareList ) {

            @Override
            public View getView( int intPositionIn, View viwConvert,
               ViewGroup vwgParent ) {
               View viwView =
                  super.getView( intPositionIn, viwConvert, vwgParent );
               TextView txtView =
                  (TextView)viwView.findViewById( android.R.id.text1 );
               txtView.setTextColor( Color.BLACK );
               return viwView;
            }
         };
      lstShares.setAdapter( adpShareAdapter );
   }

   @Override
   public boolean onCreateOptionsMenu( Menu menu ) {
      // Inflate the menu; this adds items to the action bar if it is present.
      getMenuInflater().inflate( R.menu.combine, menu );
      return true;
   }

   @Override
   public void onActivityResult(
      int intRequestCode, int intResultCode, Intent iteIntent
   ) {
      IntentResult scrScanResult =
         IntentIntegrator.parseActivityResult( intRequestCode, intResultCode, iteIntent );
      if( null != scrScanResult ) {
         if( RESULT_OK == intResultCode ) {
            String strContents = iteIntent.getStringExtra( "SCAN_RESULT" );
            
            // Split up the share and number.
            try {
               String strShareString;
               int intShareNum;
               Pattern ptnShare = Pattern.compile( "([0-9]+) (bigintcs:[A-Za-z0-9\\-]+)" );
               Matcher mtcShare = ptnShare.matcher( strContents );
               if( mtcShare.find() ) {
                  strShareString = mtcShare.group( 2 );
                  intShareNum = Integer.parseInt( mtcShare.group( 1 ) );
               } else {
                  // TODO: Use a resource string.
                  throw new SecretShareException( "Output parsing failed." );
               }
               
               // Place the returned code in the add share edit box.
               final EditText txtShareString =
                  (EditText)findViewById( R.id.txtShareString );
               txtShareString.setText( strShareString );
               final EditText txtShareNum =
                  (EditText)findViewById( R.id.txtShareNum );
               txtShareNum.setText( Integer.toString( intShareNum ) );
            } catch( NumberFormatException ex ) {
               //Toast.makeText(
               //   this, ex.getMessage(), Toast.LENGTH_LONG
               //).show();
               Toast.makeText(
                  // TODO: Use a resource string.
                  this, "Unable to parse scanned share.", Toast.LENGTH_LONG
               ).show();
            }

         } else if( RESULT_CANCELED == intResultCode ) {
            // TODO: Handle cancel.
         }
      }
   }

   @SuppressWarnings("unchecked")
   public void addShare( int intShareNumIn, String strShareStringIn ) {
      caShrShareList.add( new ShareData( intShareNumIn, strShareStringIn ) );
      final ListView lstShares = (ListView)findViewById( R.id.lstShares );
      ((ArrayAdapter<ShareData>)lstShares.getAdapter()).notifyDataSetChanged();
   }

   public String combineTAShares( ArrayList<ShareData> aShrShareDataIn ) {
      String strOutputSecret = null;

      // Build a command line from the share list.
      ArrayList<String> aStrCommandLine = new ArrayList<String>();
      aStrCommandLine.add( "-k" );
      // TODO: Implement a way to detect the threshold dynamically.
      aStrCommandLine.add( Integer.toString( aShrShareDataIn.size() ) );

      // Add each share to the command line.
      for( ShareData shrIter : aShrShareDataIn ) {
         aStrCommandLine.add( String.format( "-s%d", shrIter.cIntShareNum ) );
         aStrCommandLine.add( shrIter.cStrShareString );
      }

      try {
         ByteArrayOutputStream osOutput = new ByteArrayOutputStream();
         PrintStream psOutput = new PrintStream( osOutput );

         // Combine the shares and print them to a string.
         String[] aStrCommandLineRO = new String[aStrCommandLine.size()];
         aStrCommandLine.toArray( aStrCommandLineRO );
         CombineInput ssssInput = CombineInput.parse( aStrCommandLineRO );
         CombineOutput ssssOutput = ssssInput.output();
         ssssOutput.print( psOutput );

         // We could conceivably use subclassing if the secret hadn't been
         // marked "private". There might be a better way to do this but
         // quick and dirty should work for now. Feel free to submit
         // improvements!
         String strOutput = new String( osOutput.toByteArray(), "UTF-8" );
         // Toast.makeText( this, strOutput, Toast.LENGTH_LONG ).show();
         Pattern ptnOutput = Pattern.compile( "secret.string = '(.+?)'" );
         Matcher mtcOutput = ptnOutput.matcher( strOutput );
         if( mtcOutput.find() ) {
            strOutputSecret = mtcOutput.group( 1 );
         } else {
            // TODO: Use a resource string.
            throw new SecretShareException( "Output parsing failed." );
         }

         // Toast.makeText( this, strOutputSecret, Toast.LENGTH_LONG ).show();
      } catch( Exception ex ) {
         Toast.makeText( this, ex.getMessage(), Toast.LENGTH_LONG ).show();
      }
      return strOutputSecret;
   }
}
