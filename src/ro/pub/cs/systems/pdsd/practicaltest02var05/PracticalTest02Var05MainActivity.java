package ro.pub.cs.systems.pdsd.practicaltest02var05;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.Timestamp;
import java.util.HashMap;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.app.Activity;
import android.os.Bundle;
import android.provider.SyncStateContract.Constants;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class PracticalTest02Var05MainActivity extends Activity {
	
	EditText serverPortEditText;
	EditText commandEditText;
	TextView resultTextView;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practical_test02_var05_main);
        
        serverPortEditText = (EditText) findViewById(R.id.server_port_edit_text);
        commandEditText = (EditText) findViewById(R.id.client_command);
        resultTextView = (TextView) findViewById(R.id.result);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.practical_test02_var05_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
       
    private class MemoryInformation {
    	String word;
    	Timestamp time;
    	
    	MemoryInformation(String word, Timestamp time) {
    		this.word = word;
    		this.time = time;
    	}
    }
    
    private class ClientThread extends Thread {
    	
    	String address;
    	String port;
    	
    	Socket socket;
    	
    	ClientThread() {
    		this.address = address;
    		this.port = port;
    	}
    
    	@Override
    	public void run() {
    	  try {
    	    socket = new Socket(address, port);
    	    if (socket == null) {
    	      Log.e("PracticalTest", "[CLIENT THREAD] Could not create socket!");
    	      return;
    	    }
    	    BufferedReader bufferedReader = Utilities.getReader(socket);
    	    PrintWriter    printWriter    = Utilities.getWriter(socket);
    	    if (bufferedReader != null && printWriter != null) {
    	      String status;
    	      status = bufferedReader.readLine();
    	      if ( status != null )
    	      {
    	        final String finalizedAnser = status;
    	        resultTextView.post(new Runnable() {
    	          @Override
    	          public void run() {
    	            resultTextView.setText(status);
    	          }
    	        });
    	      }
    	    } else {
    	      Log.e("PracticalTest", "[CLIENT THREAD] BufferedReader / PrintWriter are null!");
    	    }
    	    socket.close();
    	  } catch (IOException ioException) {
    	    Log.e("PracticalTest", "[CLIENT THREAD] An exception has occurred: " + ioException.getMessage());
    	  }
    	}
    	
    }
    
    private class CommunicationThread extends Thread {
    	ServerThread serverThread;
    	Socket socket;
    	CommunicationThread(ServerThread serverThread, Socket socket) {
    		this.serverThread = serverThread;
    		this.socket = socket;
    	}
    	
    	@Override
    	public void run() {
    	  if (socket != null) {
    	    try {
    	      BufferedReader bufferedReader = Utilities.getReader(socket);
    	      PrintWriter    printWriter    = Utilities.getWriter(socket);
    	      if (bufferedReader != null && printWriter != null) {
    	        Log.i("PracticalTest", "[COMMUNICATION THREAD] Waiting for parameters from client (city / information type)!");
    	        String command = bufferedReader.readLine();
    	        
    	        HashMap<String, MemoryInformation> data = serverThread.getData();
    	        MemoryInformation memoryInformation = null;
    	        
    	        String[] words = command.split(",");
    	        
    	        if (words[0] != null && words[0].equals("put") ) {
    	          if (!data.containsKey(words[1])) {
    	            Log.i("PracticalTest", "[COMMUNICATION THREAD] Getting the information from the cache...");
    	            	HttpClient httpClient = new DefaultHttpClient();
    	            	HttpGet httpWebPageGet = new HttpGet("http://www.timeapi.org/utc/now");
    	            	ResponseHandler<String> responseHandler = new BasicResponseHandler();
    	            	try {
    	            		String pageSourceCode = httpClient.execute(httpWebPageGet, responseHandler);
    	            		String response = pageSourceCode.split("[-T:+]");
    	            		Timestamp time;
    	    	        	data.put(words[1], new MemoryInformation(words[2],time));
    	    	        	serverThread.setData(answer, "inserted\n");
    	            		
    	            	}
    	            	catch ( ClientProtocolException clientProtocolException ) {
    	            	
    	            	}
    	          }
    	          else
    	          {
    	        	memoryInformation = data.get(words[1]);
  	            	HttpClient httpClient = new DefaultHttpClient();
  	            	HttpGet httpWebPageGet = new HttpGet("http://www.timeapi.org/utc/now");
  	            	ResponseHandler<String> responseHandler = new BasicResponseHandler();
  	            	try {
  	            		String pageSourceCode = httpClient.execute(httpWebPageGet, responseHandler);
  	            		String response = pageSourceCode.split("[-T:+]");
  	            		Timestamp time;
  	    	        	data.put(words[1], new MemoryInformation(words[2],time));
  	    	        	serverThread.setData(answer, "inserted");
  	    	        	if ( time - memoryInformation.time > 60 )
  	    	        	{
  	    	        		data.put(words[1], new MemoryInformation(words[2],time));
  	    	        		serverThread.setData(answer, "inserted\n");  	    	        		
  	    	        	}
  	    	        	else
  	    	        	{
  	    	        		data.put(words[1], new MemoryInformation(words[2],time));
  	    	        		serverThread.setData(answer, "modified\n");  
  	    	        	}
  	            		
  	            	}
  	            	catch ( ClientProtocolException clientProtocolException ) {
  	            	
  	            	}
    	          }
    	        }
    	        else if(words[0] != null && words[0].equals("get"))
    	        {
    	        	if (!data.containsKey(words[1])) {
    	        		serverThread.setData(answer, data.get(words[1]).word);  
    	        	}
    	        	else
    	        	{
    	        		serverThread.setData(answer, "none\n");  
    	        	}
    	        }
    	      }
    	    }
    	  } 
    	}
    }      
 
    
    private class ServerThread extends Thread {
       	
    	private boolean isRunning;
     	ServerSocket serverSocket;
    	
    	public ServerThread(int port) {
    		isRunning = true;
    	    start();
    	}
    	    	
    	public void stopServer() {
    	    isRunning = false;
    	    new Thread(new Runnable() {
    	      @Override
    	      public void run() {
    	        try {
    	          if (serverSocket != null) {
    	            serverSocket.close();
    	          }
    	          Log.v("PracticalTest", "stopServer() method invoked "+serverSocket);
    	        } catch(IOException ioException) {
    	          Log.e("PracticalTest", "An exception has occurred: "+ioException.getMessage());
    	        }
    	      }
    	    }).start();
    	  }
    	
    	HashMap data = new HashMap<String, MemoryInformation>();
    	
	    @Override
	    public void run() {
	      try {
	        while (!Thread.currentThread().isInterrupted()) {
	          Log.i("PracticalTest", "[SERVER] Waiting for a connection...");
	          Socket socket = serverSocket.accept();
	          Log.i("PracticalTest", "[SERVER] A connection request was received from " + socket.getInetAddress() + ":" + socket.getLocalPort());
	          CommunicationThread communicationThread = new CommunicationThread(this, socket);
	          communicationThread.start();
	        }
	      } catch (ClientProtocolException clientProtocolException) {
	        Log.e("PracticalTest", "An exception has occurred: " + clientProtocolException.getMessage());
	      } catch (IOException ioException) {
	        Log.e("PracticalTest", "An exception has occurred: " + ioException.getMessage());
	      }
	    }
	    
	    public void stopThread() {
	    	  if (serverSocket != null) {
	    	    interrupt();
	    	    try {
	    	      if (serverSocket != null) {
	    	        serverSocket.close();
	    	      }
	    	    } catch (IOException ioException) {
	    	      Log.e("PracticalTest", "An exception has occurred: " + ioException.getMessage());
	    	      }
	    	    }
	    	  }
	    public ServerSocket getServerSocket () {
	    		return serverSocket;
	    	}
	    
	    }
    
    ServerThread serverThread;
    
	private class ConnectButtonClickListener implements Button.OnClickListener {
		  @Override
		  public void onClick(View view) {
		    String serverPort = serverPortEditText.getText().toString();
		    if (serverPort == null || serverPort.isEmpty()) {
		      Toast.makeText(
		        getApplicationContext(),
		        "Server port should be filled!",
		        Toast.LENGTH_SHORT
		      ).show();
		      return;
		    }
		    serverThread = new ServerThread(Integer.parseInt(serverPort));
		    if (serverThread.getServerSocket() != null) {
		      serverThread.start();
		    } else {
		      Log.e("PracticalTest", "[MAIN ACTIVITY] Could not creat server thread!");
		    }
		  }
		}
	
	private class GetWeatherForecastButtonClickListener implements Button.OnClickListener {
		  @Override
		  public void onClick(View view) {
		    String command = commandEditText.getText().toString();
		    
		    if (command == null || command.isEmpty()) {
		      Toast.makeText(
		        getApplicationContext(),
		        "Client connection parameters should be filled!",
		        Toast.LENGTH_SHORT
		      ).show();
		      return;
		    }
		    if (serverThread == null || !serverThread.isAlive()) {
		      Log.e(Constants.TAG, "[MAIN ACTIVITY] There is no server to connect to!");
		      return;
		    }
		    resultTextView.setText("");
		    clientThread = new ClientThread(
		      "localhost", 
		    clientThread.start();
		  }
		}
	    
    @Override
    protected void onDestroy() {
      if (serverThread != null) {
        serverThread.stopThread();
      }
      super.onDestroy();
    }
    
    
}
