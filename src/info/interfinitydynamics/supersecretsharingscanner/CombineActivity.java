package info.interfinitydynamics.supersecretsharingscanner;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.tiemens.secretshare.exceptions.SecretShareException;
import com.tiemens.secretshare.main.cli.MainCombine.CombineOutput;
import com.tiemens.secretshare.main.cli.MainCombine.CombineInput;

import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

      // Populate the share listbox.
      final ListView lstShares = (ListView)findViewById( R.id.lstShares );
      ArrayAdapter<ShareData> adpShareAdapter = 
         new ArrayAdapter<CombineActivity.ShareData>(
            getApplicationContext(),
            android.R.layout.simple_list_item_1,
            caShrShareList
         ) {

            @Override
            public View getView(
               int intPositionIn,
               View viwConvert,
               ViewGroup vwgParent
            ) {
                View viwView = super.getView(
                   intPositionIn, viwConvert, vwgParent
                );
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
      aStrCommandLine.add( "3" );
      
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
