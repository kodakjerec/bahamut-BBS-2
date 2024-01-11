package com.kumi.Telnet;

import java.io.IOException;

public class TelnetConnector implements TelnetChannelListener {
  private TelnetChannel[] _channel = new TelnetChannel[2];
  
  private HolderThread _holder_thread = null;
  
  private boolean _is_connecting = false;
  
  private long _last_send_data_time = 0L;
  
  private TelnetConnectorListener _listener = null;
  
  private TelnetSocketChannel _socket_channel = null;
  
  private void sendHoldMessage() {
    TelnetOutputBuilder.create().pushData((byte)27).pushData((byte)91).pushData((byte)65).pushData((byte)27).pushData((byte)91).pushData((byte)66).sendToServer();
  }
  
  public void cleanReadDataSize() {
    TelnetChannel telnetChannel = getChannel(0);
    if (telnetChannel != null)
      telnetChannel.cleanReadDataSize(); 
  }
  
  public void clear() {
    // Byte code:
    //   0: aload_0
    //   1: monitorenter
    //   2: aload_0
    //   3: getfield _channel : [Lcom/kumi/Telnet/TelnetChannel;
    //   6: iconst_0
    //   7: aconst_null
    //   8: aastore
    //   9: aload_0
    //   10: getfield _channel : [Lcom/kumi/Telnet/TelnetChannel;
    //   13: iconst_1
    //   14: aconst_null
    //   15: aastore
    //   16: aload_0
    //   17: monitorexit
    //   18: aload_0
    //   19: getfield _holder_thread : Lcom/kumi/Telnet/TelnetConnector$HolderThread;
    //   22: ifnull -> 32
    //   25: aload_0
    //   26: getfield _holder_thread : Lcom/kumi/Telnet/TelnetConnector$HolderThread;
    //   29: invokevirtual close : ()V
    //   32: aload_0
    //   33: aconst_null
    //   34: putfield _holder_thread : Lcom/kumi/Telnet/TelnetConnector$HolderThread;
    //   37: aload_0
    //   38: getfield _socket_channel : Lcom/kumi/Telnet/TelnetSocketChannel;
    //   41: ifnull -> 54
    //   44: aload_0
    //   45: getfield _socket_channel : Lcom/kumi/Telnet/TelnetSocketChannel;
    //   48: invokeinterface finishConnect : ()Z
    //   53: pop
    //   54: aload_0
    //   55: aconst_null
    //   56: putfield _socket_channel : Lcom/kumi/Telnet/TelnetSocketChannel;
    //   59: aload_0
    //   60: iconst_0
    //   61: putfield _is_connecting : Z
    //   64: aload_0
    //   65: lconst_0
    //   66: putfield _last_send_data_time : J
    //   69: return
    //   70: astore_1
    //   71: aload_0
    //   72: monitorexit
    //   73: aload_1
    //   74: athrow
    //   75: astore_1
    //   76: aload_1
    //   77: invokevirtual printStackTrace : ()V
    //   80: goto -> 54
    // Exception table:
    //   from	to	target	type
    //   2	18	70	finally
    //   44	54	75	java/io/IOException
    //   71	73	70	finally
  }
  
  public void close() {
    clear();
    if (this._listener != null)
      this._listener.onTelnetConnectorClosed(this); 
  }
  
  public void connect(String paramString, int paramInt) {
    // Byte code:
    //   0: aload_0
    //   1: invokevirtual isConnecting : ()Z
    //   4: ifeq -> 11
    //   7: aload_0
    //   8: invokevirtual close : ()V
    //   11: aload_0
    //   12: getfield _listener : Lcom/kumi/Telnet/TelnetConnectorListener;
    //   15: ifnull -> 28
    //   18: aload_0
    //   19: getfield _listener : Lcom/kumi/Telnet/TelnetConnectorListener;
    //   22: aload_0
    //   23: invokeinterface onTelnetConnectorConnectStart : (Lcom/kumi/Telnet/TelnetConnector;)V
    //   28: aload_0
    //   29: iconst_0
    //   30: putfield _is_connecting : Z
    //   33: getstatic java/lang/System.out : Ljava/io/PrintStream;
    //   36: astore #4
    //   38: new java/lang/StringBuilder
    //   41: astore_3
    //   42: aload_3
    //   43: invokespecial <init> : ()V
    //   46: aload #4
    //   48: aload_3
    //   49: ldc 'Connect to '
    //   51: invokevirtual append : (Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   54: aload_1
    //   55: invokevirtual append : (Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   58: ldc ':'
    //   60: invokevirtual append : (Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   63: iload_2
    //   64: invokevirtual append : (I)Ljava/lang/StringBuilder;
    //   67: invokevirtual toString : ()Ljava/lang/String;
    //   70: invokevirtual println : (Ljava/lang/String;)V
    //   73: new com/kumi/Telnet/TelnetDefaultSocketChannel
    //   76: astore_3
    //   77: aload_3
    //   78: aload_1
    //   79: iload_2
    //   80: invokespecial <init> : (Ljava/lang/String;I)V
    //   83: aload_0
    //   84: aload_3
    //   85: putfield _socket_channel : Lcom/kumi/Telnet/TelnetSocketChannel;
    //   88: aload_0
    //   89: monitorenter
    //   90: aload_0
    //   91: getfield _channel : [Lcom/kumi/Telnet/TelnetChannel;
    //   94: astore_1
    //   95: new com/kumi/Telnet/TelnetChannel
    //   98: astore_3
    //   99: aload_3
    //   100: aload_0
    //   101: getfield _socket_channel : Lcom/kumi/Telnet/TelnetSocketChannel;
    //   104: invokespecial <init> : (Lcom/kumi/Telnet/TelnetSocketChannel;)V
    //   107: aload_1
    //   108: iconst_0
    //   109: aload_3
    //   110: aastore
    //   111: aload_0
    //   112: getfield _channel : [Lcom/kumi/Telnet/TelnetChannel;
    //   115: iconst_0
    //   116: aaload
    //   117: aload_0
    //   118: invokevirtual setListener : (Lcom/kumi/Telnet/TelnetChannelListener;)V
    //   121: aload_0
    //   122: getfield _channel : [Lcom/kumi/Telnet/TelnetChannel;
    //   125: astore_1
    //   126: new com/kumi/Telnet/TelnetChannel
    //   129: astore_3
    //   130: aload_3
    //   131: aload_0
    //   132: getfield _socket_channel : Lcom/kumi/Telnet/TelnetSocketChannel;
    //   135: invokespecial <init> : (Lcom/kumi/Telnet/TelnetSocketChannel;)V
    //   138: aload_1
    //   139: iconst_1
    //   140: aload_3
    //   141: aastore
    //   142: aload_0
    //   143: getfield _channel : [Lcom/kumi/Telnet/TelnetChannel;
    //   146: iconst_1
    //   147: aaload
    //   148: aload_0
    //   149: invokevirtual setListener : (Lcom/kumi/Telnet/TelnetChannelListener;)V
    //   152: aload_0
    //   153: monitorexit
    //   154: aload_0
    //   155: iconst_1
    //   156: putfield _is_connecting : Z
    //   159: aload_0
    //   160: getfield _is_connecting : Z
    //   163: ifeq -> 221
    //   166: aload_0
    //   167: getfield _listener : Lcom/kumi/Telnet/TelnetConnectorListener;
    //   170: ifnull -> 183
    //   173: aload_0
    //   174: getfield _listener : Lcom/kumi/Telnet/TelnetConnectorListener;
    //   177: aload_0
    //   178: invokeinterface onTelnetConnectorConnectSuccess : (Lcom/kumi/Telnet/TelnetConnector;)V
    //   183: aload_0
    //   184: new com/kumi/Telnet/TelnetConnector$HolderThread
    //   187: dup
    //   188: aload_0
    //   189: aconst_null
    //   190: invokespecial <init> : (Lcom/kumi/Telnet/TelnetConnector;Lcom/kumi/Telnet/TelnetConnector$1;)V
    //   193: putfield _holder_thread : Lcom/kumi/Telnet/TelnetConnector$HolderThread;
    //   196: aload_0
    //   197: getfield _holder_thread : Lcom/kumi/Telnet/TelnetConnector$HolderThread;
    //   200: invokevirtual start : ()V
    //   203: return
    //   204: astore_1
    //   205: aload_0
    //   206: monitorexit
    //   207: aload_1
    //   208: athrow
    //   209: astore_1
    //   210: aload_1
    //   211: invokevirtual printStackTrace : ()V
    //   214: aload_0
    //   215: invokevirtual clear : ()V
    //   218: goto -> 159
    //   221: aload_0
    //   222: getfield _listener : Lcom/kumi/Telnet/TelnetConnectorListener;
    //   225: ifnull -> 203
    //   228: aload_0
    //   229: getfield _listener : Lcom/kumi/Telnet/TelnetConnectorListener;
    //   232: aload_0
    //   233: invokeinterface onTelnetConnectorConnectFail : (Lcom/kumi/Telnet/TelnetConnector;)V
    //   238: goto -> 203
    // Exception table:
    //   from	to	target	type
    //   33	90	209	java/io/IOException
    //   90	107	204	finally
    //   111	138	204	finally
    //   142	154	204	finally
    //   154	159	209	java/io/IOException
    //   205	207	204	finally
    //   207	209	209	java/io/IOException
  }
  
  protected void finalize() throws Throwable {
    if (isConnecting())
      close(); 
    super.finalize();
  }
  
  TelnetChannel getChannel(int paramInt) {
    // Byte code:
    //   0: aload_0
    //   1: monitorenter
    //   2: aload_0
    //   3: getfield _channel : [Lcom/kumi/Telnet/TelnetChannel;
    //   6: ifnull -> 20
    //   9: aload_0
    //   10: getfield _channel : [Lcom/kumi/Telnet/TelnetChannel;
    //   13: iload_1
    //   14: aaload
    //   15: astore_2
    //   16: aload_0
    //   17: monitorexit
    //   18: aload_2
    //   19: areturn
    //   20: aconst_null
    //   21: astore_2
    //   22: aload_0
    //   23: monitorexit
    //   24: goto -> 18
    //   27: astore_2
    //   28: aload_0
    //   29: monitorexit
    //   30: aload_2
    //   31: athrow
    // Exception table:
    //   from	to	target	type
    //   2	18	27	finally
    //   22	24	27	finally
    //   28	30	27	finally
  }
  
  public int getReadDataSize() {
    int i = 0;
    TelnetChannel telnetChannel = getChannel(0);
    if (telnetChannel != null)
      i = telnetChannel.getReadDataSize(); 
    return i;
  }
  
  public boolean isConnecting() {
    return this._is_connecting;
  }
  
  public void lockChannel(int paramInt) {
    TelnetChannel telnetChannel = getChannel(paramInt);
    if (telnetChannel != null)
      telnetChannel.lock(); 
  }
  
  public void onTelnetChannelReceiveDataFinished(TelnetChannel paramTelnetChannel) {
    if (this._listener != null)
      this._listener.onTelnetConnectorReceiveDataFinished(this); 
  }
  
  public void onTelnetChannelReceiveDataStart(TelnetChannel paramTelnetChannel) {
    if (this._listener != null)
      this._listener.onTelnetConnectorReceiveDataStart(this); 
  }
  
  public byte readData(int paramInt) throws TelnetConnectionClosedException, IOException {
    TelnetChannel telnetChannel = getChannel(paramInt);
    return (telnetChannel != null) ? telnetChannel.readData() : 0;
  }
  
  public void sendData(int paramInt) {
    TelnetChannel telnetChannel = getChannel(paramInt);
    if (telnetChannel != null && telnetChannel.sendData())
      this._last_send_data_time = System.currentTimeMillis(); 
  }
  
  public void setListener(TelnetConnectorListener paramTelnetConnectorListener) {
    this._listener = paramTelnetConnectorListener;
  }
  
  public void undoReadData(int paramInt) {
    TelnetChannel telnetChannel = getChannel(paramInt);
    if (telnetChannel != null)
      telnetChannel.undoReadData(); 
  }
  
  public void unlockChannel(int paramInt) {
    TelnetChannel telnetChannel = getChannel(paramInt);
    if (telnetChannel != null) {
      telnetChannel.unlock();
      sendData(paramInt);
    } 
  }
  
  public void writeData(byte paramByte, int paramInt) {
    TelnetChannel telnetChannel = getChannel(paramInt);
    if (telnetChannel != null)
      telnetChannel.writeData(paramByte); 
  }
  
  public void writeData(byte[] paramArrayOfbyte, int paramInt) {
    TelnetChannel telnetChannel = getChannel(paramInt);
    if (telnetChannel != null)
      telnetChannel.writeData(paramArrayOfbyte); 
  }
  
  private class HolderThread extends Thread {
    private boolean _run = true;
    
    final TelnetConnector this$0;
    
    private HolderThread() {}
    
    public void close() {
      this._run = false;
    }
    
    public void run() {
      while (this._run && TelnetConnector.this._holder_thread == this) {
        try {
          sleep(30000L);
        } catch (InterruptedException interruptedException) {
          interruptedException.printStackTrace();
        } 
        if (System.currentTimeMillis() - TelnetConnector.this._last_send_data_time > 150000L && TelnetConnector.this._holder_thread == this)
          TelnetConnector.this.sendHoldMessage(); 
      } 
    }
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Telnet\TelnetConnector.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */