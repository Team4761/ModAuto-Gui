package transfer;

import com.jcraft.jsch.UserInfo;

public class SftpUserInfo implements UserInfo {
    @Override
    public String getPassphrase() {
        return null;
    }

    @Override
    public String getPassword() {
        return "1234";
    }

    @Override
    public boolean promptPassword(String message) {
        return true;
    }

    @Override
    public boolean promptPassphrase(String message) {
        return true;
    }

    @Override
    public boolean promptYesNo(String message) {
        return true;
    }

    @Override
    public void showMessage(String message) {

    }
}
