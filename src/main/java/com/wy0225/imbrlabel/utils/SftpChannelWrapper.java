package com.wy0225.imbrlabel.utils;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;

/**
 * SFTP通道包装类，实现AutoCloseable接口
 */
public class SftpChannelWrapper implements AutoCloseable {
    private final ChannelSftp channel;

    public SftpChannelWrapper(Session session) throws Exception {
        Channel channel= session.openChannel("sftp");
        channel.connect();
        this.channel = (ChannelSftp) channel;
    }

    public ChannelSftp getChannel() {
        return channel;
    }

    @Override
    public void close() {
        if (channel != null && channel.isConnected()) {
            channel.disconnect();
        }
    }
}
