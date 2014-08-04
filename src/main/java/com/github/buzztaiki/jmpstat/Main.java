package com.github.buzztaiki.jmpstat;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

public class Main {
    private void usage(PrintStream out) {
        out.println("Usage: jmpstat <host>:<port> [<pool_names> <interval>]");
    }

    private int run(Args args) throws Exception {
        String conn = args.get(0);
        if (conn == null) {
            usage(System.err);
            return 1;
        }
        try (JMXConnector jmxConn = JMXConnectorFactory.connect(getUrl(conn))) {
            JmpStat jmpStat = new JmpStat(jmxConn.getMBeanServerConnection(), System.out);
            Set<String> poolNames = poolNames(args.get(1, ""));
            if (poolNames.isEmpty()) {
                jmpStat.printAllPools();
            } else {
                if (!jmpStat.hasMemoryPool(poolNames)) {
                    System.err.println("Invalid pool_names: " + args.get(1));
                    usage(System.err);
                    return 1;
                }
                long interval = Long.parseLong(args.get(2, "1000"));
                jmpStat.pollLoop(poolNames, interval);
            }
            return 0;
        }
    }

    private Set<String> poolNames(String arg) {
        Set<String> xs = new HashSet<>(Arrays.asList(arg.split(" *, *", 0)));
        xs.remove("");
        return xs;
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
        System.exit(new Main().run(Args.of(args)));
    }
}
