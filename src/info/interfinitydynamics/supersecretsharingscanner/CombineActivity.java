package info.interfinitydynamics.supersecretsharingscanner;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.tiemens.secretshare.exceptions.SecretShareException;
import com.tiemens.secretshare.main.cli.MainCombine.CombineOutput;
import com.tiemens.secretshare.main.cli.MainCombine.CombineInput;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class CombineActivity extends Activity {
   // Test string: "the number of shares to generate"
   private String[] caShareArray = {
      "-k", "3",
      "-s1", "bigintcs:007468-65206e-756d62-657220-6f6620-736875-dfbe54-c94a99-ad0896-03dd4e-316518-77FA28",
      "-s3", "bigintcs:007468-65206e-756d62-657220-6f6620-736902-f59561-08d2f1-68912f-2f9a6a-a5c4c4-18E0E9",
      "-s4", "bigintcs:007468-65206e-756d62-657220-6f6620-73697b-9e138b-9f851e-977897-c5dfab-4a33bd-03161E",
   };

   @Override
   protected void onCreate( Bundle savedInstanceState ) {
      super.onCreate( savedInstanceState );
      setContentView( R.layout.activity_combine );
      
      final Button btnCombine = (Button)findViewById( R.id.btnCombine );
      btnCombine.setOnClickListener( new View.OnClickListener() {
          public void onClick(View v) {
             combineTAShares();
          }
      } );
   }

   @Override
   public boolean onCreateOptionsMenu( Menu menu ) {
      // Inflate the menu; this adds items to the action bar if it is present.
      getMenuInflater().inflate( R.menu.combine, menu );
      return true;
   }

   public String combineTAShares() {
      String strOutputSecret = null;
      
      //EditText txtShares = (EditText)findViewById( R.id.txtShares );
      
      //String[] aStrShares = txtShares.getText().toString().split( "[\\r\\n]+" );
      
      // TODO: Prune any arguments or non-shares from the text area.
      try {
         ByteArrayOutputStream osOutput = new ByteArrayOutputStream();
         PrintStream psOutput = new PrintStream( osOutput );
         
         // Combine the shares and print them to a string.
         CombineInput ssssInput = CombineInput.parse( caShareArray );
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
      } catch( SecretShareException ex ) {
         Toast.makeText( this, ex.getMessage(), Toast.LENGTH_LONG ).show();
      } catch( UnsupportedEncodingException ex ) {
         Toast.makeText( this, ex.getMessage(), Toast.LENGTH_LONG ).show();
      }
      return strOutputSecret;
   }
}
