package com.kumi.Bahamut.Command;

import com.kumi.Bahamut.ListPage.TelnetListPage;
import com.kumi.Bahamut.ListPage.TelnetListPageBlock;
import com.kumi.Telnet.TelnetClient;
import com.kumi.Telnet.TelnetOutputBuilder;

public class BahamutCommandSendMail extends TelnetCommand {
  String _content = "";
  
  String _receiver = "";
  
  String _title = "";
  
  public BahamutCommandSendMail(String paramString1, String paramString2, String paramString3) {
    this._receiver = paramString1;
    this._title = paramString2;
    this._content = paramString3;
    this.Action = 12;
  }
  
  public void execute(TelnetListPage paramTelnetListPage) {
    if (this._receiver.length() > 0 && this._title.length() > 0 && this._content.length() > 0) {
      byte[] arrayOfByte = TelnetOutputBuilder.create().pushKey(115).pushString(this._receiver + "\n").pushString(this._title + "\n").pushString("0\n").pushString(this._content).pushData((byte)24).pushString("s\nn\n\n").build();
      TelnetClient.getClient().sendDataToServer(arrayOfByte);
    } 
  }
  
  public void executeFinished(TelnetListPage paramTelnetListPage, TelnetListPageBlock paramTelnetListPageBlock) {
    setDone(true);
  }
  
  public String toString() {
    return "[SendMail][title=" + this._title + " receiver=" + this._receiver + " content=" + this._content + "]";
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\Bahamut\Command\BahamutCommandSendMail.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */