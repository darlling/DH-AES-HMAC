import java.io.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.net.Socket;
import java.util.Base64;
import java.util.Map;
 
public class Client extends JFrame implements ActionListener
{
   /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Socket fclient;
	Socket sclient;
	Socket client;
	boolean done;

	JButton sent;
	JTextArea chatContent;
	JTextField sentence;
	DataInputStream in = null;
	DataOutputStream out = null;
	DataInputStream fin = null;
	DataOutputStream sout = null;
	byte[] AESKey;
    final Base64.Encoder encoder = Base64.getEncoder();	// ���ֽ�����ת��Ϊbase64����
    final Base64.Decoder decoder = Base64.getDecoder();	// ��base64�����ַ���ת�����ֽ�����
   
	public Client()
	{
		buildGUI("Client");  // �ͻ�����
		try
		{
			fclient = new Socket("localhost",8124);
			fin = new DataInputStream(fclient.getInputStream());
			done = false;
			String line;
			String check;
			chatContent.append("�ɹ����ӵ������...\n���Ӷ˿ںţ�8124\n");
		
			new DH_AES_HMAC();
			// ���շ���˹�Կ
			byte[] serverPublicKey = fin.readAllBytes();
			// �ɷ���˹�Կ�����Ŀͻ�����Կ��
	        Map<String,Object> keyMap2=DH_AES_HMAC.initKey(serverPublicKey);
	        // �ͻ��˹�Կ
	        byte[] publicKey2=DH_AES_HMAC.getPublicKey(keyMap2);
	        // �ͻ���˽Կ
	        byte[] privateKey2=DH_AES_HMAC.getPrivateKey(keyMap2);
	        chatContent.append("****************************************************\n");
	        chatContent.append("���յ�����˷��͵Ĺ�Կ��\n"+encoder.encodeToString(serverPublicKey));
	        chatContent.append("\n�ͻ��˹�Կ��\n"+encoder.encodeToString(publicKey2));
	        chatContent.append("\n�ͻ���˽Կ��\n"+encoder.encodeToString(privateKey2));
	
	        // ���ͻ��˹�Կ���͸������
			sclient = new Socket("localhost",8124);
			sout = new DataOutputStream(sclient.getOutputStream());
	        sout.write(publicKey2);
	        sout.close();
	                
	        // ��װ�ͻ��˱�����Կ���ɷ���˹�Կ�Ϳͻ���˽Կ��϶���
	        // ����Կ��Ϊ�������ͻ��˾��� DH Э��Э�̳��Ĺ�����Կ
			AESKey = DH_AES_HMAC.getSecretKey(serverPublicKey, privateKey2);
			chatContent.append("\n�ɷ���˹�Կ�Ϳͻ���˽Կ���ɵĹ�����Կ��DHЭ�飩��\n"+encoder.encodeToString(AESKey)+"\n");
			
			
			client = new Socket("localhost",8124);
			out = new DataOutputStream(client.getOutputStream());
			in = new DataInputStream(client.getInputStream());
			
			while(!done)
			{
				while((line = in.readUTF())!=null)
				{
					chatContent.append("\n****************************************************\n");
					chatContent.append("���յ�����˷��������ģ�"+line+"\n");
					byte[] ci = decoder.decode(line);
					if((check = in.readUTF())!=null)
					{
						chatContent.append("���յ�����˷�������Ϣ��֤�룺"+check+"\n");
						String checksum = encoder.encodeToString(DH_AES_HMAC.HMACEncode(ci, AESKey));
						chatContent.append("�� HMAC �������Ϣ��֤�룺"+checksum+"\n");
						boolean same = check.equals(checksum);
						chatContent.append("��Ϣ��֤���Ƿ�һ�£�"+same+"\n");
						if(same) {
							chatContent.append("������Ϣ��������Դ�ɿ�...\n�� AES ���ܳ����ģ�"+new String(DH_AES_HMAC.AESDecrypt(ci, AESKey))+"\n");
						}
						else {
							chatContent.append("������Ϣ�����Ի���Դ���ɿ���\n");
						}
					}
					
					if(line.equals("bte"))
					{
						String msg = "\n��������������ͨ�����\n";
						msg += "���ӽ�����ȷ�ϴ˶Ի����10���Ӻ�ر�!\n";
						JOptionPane.showMessageDialog(this, msg);
						Thread.sleep(10000);
						done = true;
						break;
					}
				}
				in.close();
				out.close();			
				System.exit(0);
			}
		}
		catch (Exception e)
		{
			chatContent.append("\n�������ѹر�...\n");
		}
  }
   
	public void buildGUI(String title)
	{
		this.setTitle(title);
		this.setSize(650,800);
		Container container = this.getContentPane();
		container.setLayout(new BorderLayout());
		JScrollPane centerPane = new JScrollPane();
		chatContent = new JTextArea();
		centerPane.setViewportView(chatContent);
		container.add(centerPane,BorderLayout.CENTER);
		chatContent.setEditable(false);
		JPanel bottomPanel = new JPanel();
		sentence = new JTextField(20);
		sent = new JButton("Send");  // ����
		bottomPanel.add(new JLabel("Message")); // ������Ϣ
		bottomPanel.add(sentence);
		bottomPanel.add(sent);
		container.add(bottomPanel,BorderLayout.SOUTH);
		sent.addActionListener(this);
		sentence.addActionListener(this);
		this.setVisible(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	// ע�Ͳμ� Server.java
	public void actionPerformed(ActionEvent e)
	{
		String str = sentence.getText();
		if(str!=null&&!str.equals(""))
		{
			chatContent.append("\n****************************************************\n");
			chatContent.append("���ģ�"+str+"\n");
			try
			{
				byte[] code = DH_AES_HMAC.AESEncrypt(str.getBytes(), AESKey);
				String ciph = encoder.encodeToString(code);
				String check1 = encoder.encodeToString(DH_AES_HMAC.HMACEncode(code, AESKey));
				chatContent.append("�� AES ���ܳ����ģ�"+ciph+"\n");
				chatContent.append("�� HMAC �������Ϣ��֤�룺"+check1+"\n");
				out.writeUTF(ciph);
				out.writeUTF(check1);
			}
			catch (Exception e3)
			{
				chatContent.append("������δ����...\n");
			}
		}
		else
		{
			chatContent.append("������Ϣ����Ϊ��...\n");
		}
		sentence.setText("");
	}
   
	public static void main(String[] args)
	{
		new Client();
	}
}