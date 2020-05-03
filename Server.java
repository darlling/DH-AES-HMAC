import java.io.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.net.Socket;
import java.util.Base64;
import java.util.Map;
import java.net.ServerSocket;


public class Server extends JFrame implements ActionListener
{
   /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	ServerSocket server;//���������׽���
	Socket theClient;//��ͻ���ͨ�ŵ��׽���
	Socket ftheClient;
	Socket stheClient;
	boolean done;//ͨ���Ƿ����
	JButton sent;//���Ͱ�ť
	JTextArea chatContent;//����������
	JTextField sentence;//������Ϣ��
	DataOutputStream fout = null;
	DataInputStream sin = null;
	DataInputStream in = null;//���Կͻ��˵�������
	DataOutputStream out = null;//���͵��ͻ��˵������
	byte[] AESKey;
    final Base64.Encoder encoder = Base64.getEncoder();	// ���ֽ�����ת��Ϊbase64����
    final Base64.Decoder decoder = Base64.getDecoder();	// ��base64�����ַ���ת�����ֽ�����
   
	public Server() throws Exception
	{
		buildGUI("Server");  // ��������
		try
		{
			chatContent.append("�����˿ںţ�8124\n");
			server = new ServerSocket(8124);	//�����������׽��ֶ���
		}
		catch (IOException e)
		{
			chatContent.append("��ȷ�϶˿ں� 8124 δ��ռ�ú��������г���\n");
			chatContent.append("PS��Ҳ�����и���Դ������ָ���Ķ˿ں�\n");
			System.out.println(e);
		}   
	
		new DH_AES_HMAC();
	    try
	    {
	    	chatContent.append("�ȴ��ͻ�������...\n");
	    	ftheClient = server.accept();
	    	chatContent.append("�ͻ����ѳɹ�����...\n");
			fout = new DataOutputStream(ftheClient.getOutputStream());
			done = true;
			
			// ���ɷ������Կ��
		    Map<String,Object> keyMap1=DH_AES_HMAC.initKey();
		    // ����˹�Կ
		    byte[] publicKey1=DH_AES_HMAC.getPublicKey(keyMap1);  
		    // �����˽Կ
		    byte[] privateKey1=DH_AES_HMAC.getPrivateKey(keyMap1);
		    chatContent.append("\n****************************************************\n");
		    chatContent.append("����˹�Կ��\n"+encoder.encodeToString(publicKey1));
		    chatContent.append("\n�����˽Կ��\n"+encoder.encodeToString(privateKey1));
			
			// ������˹�Կ���͸��ͻ���
		    fout.write(publicKey1);
		    fout.close();
		    
		    // ���տͻ��˹�Կ
		    stheClient = server.accept();
			sin = new DataInputStream(stheClient.getInputStream());
		    byte[] clientPublicKey = sin.readAllBytes();
		    
		    // ��װ����˱�����Կ���ɿͻ��˹�Կ�ͷ����˽Կ��϶���
		    // ����Կ��Ϊ�������ͻ��˾��� DH Э��Э�̳��Ĺ�����Կ
		    AESKey = DH_AES_HMAC.getSecretKey(clientPublicKey, privateKey1);

		    chatContent.append("\n���յ��ͻ��˷��͵Ĺ�Կ��\n"+encoder.encodeToString(clientPublicKey));
			chatContent.append("\n�ɿͻ��˹�Կ�ͷ����˽Կ���ɵĹ�����Կ��DHЭ�飩��\n"+encoder.encodeToString(AESKey)+"\n");
			
	    }
	    catch(Exception rand){}
    

		while(true)
		{
			try
			{
				theClient = server.accept();
				out = new DataOutputStream(theClient.getOutputStream());
				in = new DataInputStream(theClient.getInputStream());
				done = true;
				String line;
				String check2;

				while(done)
				{
					while((line = in.readUTF())!=null)
					{
						chatContent.append("\n****************************************************\n");
						chatContent.append("���յ��ͻ��˷��������ģ�"+line+"\n");
						byte[] cdata = decoder.decode(line);
						if((check2 = in.readUTF())!=null)
						{
							chatContent.append("���յ��ͻ��˷�������Ϣ��֤�룺"+check2+"\n");
							String csum = encoder.encodeToString(DH_AES_HMAC.HMACEncode(cdata, AESKey));
							chatContent.append("�� HMAC �������Ϣ��֤�룺"+csum+"\n");
							boolean isSame = csum.equals(check2);
							chatContent.append("��Ϣ��֤���Ƿ�һ�£�"+isSame+"\n");
							if(isSame) {
								chatContent.append("������Ϣ��������Դ�ɿ�...\n�� AES ���ܳ����ģ�"+new String(DH_AES_HMAC.AESDecrypt(cdata, AESKey))+"\n");
							}
							else {
								chatContent.append("������Ϣ�����Ի���Դ���ɿ���\n");
							}
						}
					}
					in.close();
					out.close();
					theClient.close();
				}
			}
			catch (Exception e1)
			{
				chatContent.append("\n�ͻ��˹ر�����!\n");
			}	
		}
	}
	
	
	//����ͼ�ν���
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
		this.addWindowListener(new WindowAdapter()	//�����ڲ��������ڹرղ���
	    {
			public void windowClosing(WindowEvent e)
			{
				try
				{
					out.writeUTF("\nGoodbye!");
				}
				catch (IOException e2)
				{
					System.out.println("\n���������ڹر�...");
				}
				finally
				{
					System.exit(0);
				}
			}
	    });
	}
   
	public void actionPerformed(ActionEvent e)
	{
		String str = sentence.getText();	//��ȡ������Ϣ������������
		if(str!=null&&!str.equals(""))	//����������ݲ�Ϊ�գ�������Ϣ
		{
			chatContent.append("\n****************************************************\n");
			chatContent.append("���ģ�"+str+"\n");
			try
			{
				byte[] ciph = DH_AES_HMAC.AESEncrypt(str.getBytes(), AESKey);	// AES���ܳ�����
				String ciph64 = encoder.encodeToString(ciph);	// ������ת��Ϊbase64
				String check64 = encoder.encodeToString(DH_AES_HMAC.HMACEncode(ciph, AESKey));
				chatContent.append("�� AES ���ܳ����ģ�"+ciph64+"\n");
				chatContent.append("�� HMAC �������Ϣ��֤�룺"+check64+"\n");
				out.writeUTF(ciph64);	// ��base64��������ķ��͸��ͻ���
				out.writeUTF(check64);	// ��base64�����У���뷢�͸��ͻ��ˣ�������Ϣ�����ԺͿɿ��Լ���
			}
			catch (Exception e3)
			{
				chatContent.append("\nδ���ֿͻ���...\n");
			}
		}
		else
		{
			chatContent.append("\n������Ϣ����Ϊ��...\n");
		}
		sentence.setText("");	//���������Ϣ��������
	}
   
	public static void main(String[] args) throws Exception
	{
		new Server();
	}
}