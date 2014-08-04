package com.github.buzztaiki.jmpstat;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.util.ArrayList;
import java.util.List;
import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.lang.management.MemoryUsage;

public class JmpStat {
    private void usage(PrintStream out) {
        out.println("Usage: jccstat <host>:<port> [<pool_names> <interval>]");
    }

    private int run(Args args) throws Exception {
        String conn = args.get(0);
        if (conn == null) {
            usage(System.err);
            return 1;
        }
        long interval = Long.parseLong(args.get(1, "0"));
        if (interval <= 0) {
            usage(System.err);
            return 1;
        }

        try (JMXConnector jmxConn = JMXConnectorFactory.connect(getUrl(conn))) {
            MBeanServerConnection server = jmxConn.getMBeanServerConnection();
            System.out.println("pool_name\tinit\tused\tcommitted\tmax");
            for (;;) {
                for (MemoryPoolMXBean memoryPool : ManagementFactory.getPlatformMXBeans(server, MemoryPoolMXBean.class)) {
                    MemoryUsage usage = memoryPool.getUsage();
                    System.out.format("%s\t%d\t%d\t%d\t%d%n",
                        memoryPool.getName(),
                        usage.getInit()/1024,
                        usage.getUsed()/1024,
                        usage.getCommitted()/1024,
                        usage.getMax()/1024);
                }
                Thread.sleep(interval);
            }
        }
    }

    private JMXServiceURL getUrl(String conn) throws IOException {
        try {
            return getPidUrl(Integer.parseInt(conn));
        } catch (NumberFormatException e) {
            return new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + conn + "/jmxrmi");
        }
    }

    private JMXServiceURL getPidUrl(int pid) throws IOException {
/*
        LocalVirtualMachine vm = LocalVirtualMachine.getLocalVirtualMachine(pid);
        if (vm.isManageable()) {
            return new JMXServiceURL(vm.toUrl());
        }
        vm.startManagementAgent();
        if (vm.isManageable()) {
            return new JMXServiceURL(vm.toUrl());
        }
        throw new IOException("Could not start agent for " + pid);
*/
        throw new UnsupportedOperationException("getPidUrl");
    }

    public static void main(String[] args) throws Exception {
        System.exit(new JmpStat().run(Args.of(args)));
    }
}
