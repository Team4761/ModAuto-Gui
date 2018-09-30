package transfer;

import com.jcraft.jsch.*;

import java.io.File;

public class FTPManager {

    private JSch jsch;
    private String username;
    private String address;

    public FTPManager(String username, String address) {
        jsch = new JSch();
        this.username = username;
        this.address = address;
    }

    public FTPManager(String username, String address, String knownHosts) throws JSchException{
        this(username, address);
        jsch.setKnownHosts(knownHosts);
    }

    public void sendFile(String file, String name) throws JSchException, SftpException {
        Session session = jsch.getSession(username, address);

        UserInfo ui = new SftpUserInfo();
        session.setUserInfo(ui);

        session.connect();

        Channel channel = session.openChannel("sftp");
        channel.connect();

        ChannelSftp sftpChannel = (ChannelSftp) channel;

        sftpChannel.put(file, "/home/lvuser/" + name + ".json");

        sftpChannel.exit();
        session.disconnect();
    }
}
