package info.interfinitydynamics.supersecretsharingscanner;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.tiemens.secretshare.exceptions.SecretShareException;
import com.tiemens.secretshare.main.cli.MainCombine.CombineOutput;
import com.tiemens.secretshare.main.cli.MainCombine.CombineInput;

import android.os.Bundle;
import android.app.Activity;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

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
   
   class ShareAdapter implements SpinnerAdapter {
      ArrayList<ShareData> caShrShareData;
      
      public ShareAdapter( ArrayList<ShareData> aShrShareDataIn ) {
         caShrShareData = aShrShareDataIn;
      }

      @Override
      public int getCount() {
         return caShrShareData.size();
      }

      @Override
      public Object getItem( int intPositionIn ) {
         return caShrShareData.get( intPositionIn );
      }

      @Override
      public long getItemId( int intPositionIn ) {
         return intPositionIn;
      }

      @Override
      public int getItemViewType( int intPositionIn ) {
         return android.R.layout.simple_spinner_dropdown_item;
      }

      @Override
      public View getView(
         int intPositionIn,
         View viwConvertViewIn,
         ViewGroup vwgParentIn
      ) {
         TextView txtView = new TextView( getApplicationContext() );
         txtView.setTextColor( Color.BLACK );
         txtView.setText( caShrShareData.get( intPositionIn ).toString() );
         return txtView; 
      }

      @Override
      public int getViewTypeCount() {
         return 1;
      }

      @Override
      public boolean hasStableIds() {
         return false;
      }

      @Override
      public boolean isEmpty() {
         return false;
      }

      @Override
      public void registerDataSetObserver( DataSetObserver dsoObserverIn ) {
         // TODO Auto-generated method stub
      }

      @Override
      public void unregisterDataSetObserver( DataSetObserver dsoObserverIn ) {
         // TODO Auto-generated method stub
      }

      @Override
      public View getDropDownView(
         int intPositionIn,
         View viwConvertViewIn,
         ViewGroup vwgParentIn
      ) {
         return this.getView( intPositionIn, viwConvertViewIn, vwgParentIn );
      }
   }
   
   // Test string: "the number of shares to generate"
   /* private String[] caShareArray = {
      "-k", "3",
      "-s1", "bigintcs:007468-65206e-756d62-657220-6f6620-736875-dfbe54-c94a99-ad0896-03dd4e-316518-77FA28",
      "-s3", "bigintcs:007468-65206e-756d62-657220-6f6620-736902-f59561-08d2f1-68912f-2f9a6a-a5c4c4-18E0E9",
      "-s4", "bigintcs:007468-65206e-756d62-657220-6f6620-73697b-9e138b-9f851e-977897-c5dfab-4a33bd-03161E",
   }; */
   
   protected ArrayList<ShareData> caShrShareList = new ArrayList<ShareData>(); 

   @Override
   protected void onCreate( Bundle savedInstanceState ) {
      super.onCreate( savedInstanceState );
      setContentView( R.layout.activity_combine );
            
      final Button btnAddShare = (Button)findViewById( R.id.btnAddShare );
      btnAddShare.setOnClickListener( new View.OnClickListener() {
          public void onClick(View v) {
             // TODO: Check specified share for validity.
             
             final EditText txtShareNum =
                (EditText)findViewById( R.id.txtShareNum );
             final EditText txtShareString =
                (EditText)findViewById( R.id.txtShareString );
             try {
                addShare(
                   Integer.parseInt( txtShareNum.getText().toString() ),
                   txtShareString.getText().toString()
                );
             } catch( NumberFormatException ex ) {
                // TODO: Warn about using a numerical share ID.
                return;
             }
          }
      } );
      
      final Button btnCombine = (Button)findViewById( R.id.btnCombine );
      btnCombine.setOnClickListener( new View.OnClickListener() {
          public void onClick(View v) {
             combineTAShares( caShrShareList );
          }
      } );

      // Populate the share spinner.
      final Spinner spnShares = (Spinner)findViewById( R.id.spnShares );
      ShareAdapter adpShareAdapter = new ShareAdapter( caShrShareList );
      spnShares.setAdapter( adpShareAdapter );
      spnShares.setOnItemSelectedListener( new OnItemSelectedListener() {

         @Override
         public void onItemSelected(
            AdapterView<?> advParentIn,
            View viwViewIn,
            int intPositionIn,
            long lngIDIn
         ) {
            //ShareData shrSelected = 
            //   (ShareData)advParentIn.getItemAtPosition( intPositionIn );
         }

         @Override
         public void onNothingSelected( AdapterView<?> advParentIn ) {
         }
      } );
   }

   @Override
   public boolean onCreateOptionsMenu( Menu menu ) {
      // Inflate the menu; this adds items to the action bar if it is present.
      getMenuInflater().inflate( R.menu.combine, menu );
      return true;
   }
   
   public void addShare( int intShareNumIn, String strShareStringIn ) {
      caShrShareList.add( new ShareData( intShareNumIn, strShareStringIn ) );
   }

   public String combineTAShares( ArrayList<ShareData> aShrShareDataIn ) {
      String strOutputSecret = null;
      
      // Build a command line from the share list.
      ArrayList<String> aStrCommandLine = new ArrayList<String>();
      aStrCommandLine.add( "-k" );
      aStrCommandLine.add( "3" );
      
      //"-s1", "bigintcs:007468-65206e-756d62-657220-6f6620-736875-dfbe54-c94a99-ad0896-03dd4e-316518-77FA28",
      
      for( ShareData shrIter : aShrShareDataIn ) {
         aStrCommandLine.add( String.format( "-s%d", shrIter.cIntShareNum ) );
         aStrCommandLine.add( shrIter.cStrShareString );
      }
      //EditText txtShares = (EditText)findViewById( R.id.txtShares );
      
      //String[] aStrShares = txtShares.getText().toString().split( "[\\r\\n]+" );
      
      // TODO: Prune any arguments or non-shares from the text area.
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
         String strOutput = new String( osOutput.toByteArray(),"UTF-8" );
         // Toast.makeText( this, strOutput, Toast.LENGTH_LONG ).show();
         Pattern ptnOutput = Pattern.compile( "secret.string = '(.+?)'" );
         Matcher mtcOutput = ptnOutput.matcher( strOutput );
         if( mtcOutput.find() ) {
            strOutputSecret = mtcOutput.group( 1 );
         } else {
            throw new SecretShareException( "Output parsing failed." );
         }
         
         // Toast.makeText( this, strOutputSecret, Toast.LENGTH_LONG ).show();
      } catch( Exception ex ) {
         Toast.makeText( this, ex.getMessage(), Toast.LENGTH_LONG ).show();
      }
      return strOutputSecret;
   }
}
