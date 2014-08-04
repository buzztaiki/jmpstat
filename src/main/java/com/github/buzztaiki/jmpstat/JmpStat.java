package com.github.buzztaiki.jmpstat;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

public class JmpStat {
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
            MBeanServerConnection server = jmxConn.getMBeanServerConnection();
            Set<String> poolNames = poolNames(args.get(1, ""));
            System.out.println(poolNames.size());
            if (poolNames.isEmpty()) {
                printAllPools(server);
            } else {
                long interval = Long.parseLong(args.get(2, "1000"));
                pollLoop(server, poolNames, interval);
            }
            return 0;
        }
    }

    private Set<String> poolNames(String arg) {
        Set<String> xs = new HashSet<>(Arrays.asList(arg.split(" *, *", 0)));
        xs.remove("");
        return xs;
    }

    private void printAllPools(MBeanServerConnection server) throws IOException {
        for (MemoryPoolMXBean memoryPool : ManagementFactory.getPlatformMXBeans(server, MemoryPoolMXBean.class)) {
            System.out.format("%s\t%s%n", memoryPool.getName(), memoryPool.getUsage());
        }
    }

    private void pollLoop(MBeanServerConnection server, Set<String> poolNames, long interval) throws IOException {
        System.out.println("pool_name\tinit\tused\tcommitted\tmax");
        for (;;) {
            for (MemoryPoolMXBean memoryPool : ManagementFactory.getPlatformMXBeans(server, MemoryPoolMXBean.class)) {
                if (!poolNames.contains(memoryPool.getName())) {
                    continue;
                }
                MemoryUsage usage = memoryPool.getUsage();
                System.out.format("%s\t%d\t%d\t%d\t%d%n",
                    memoryPool.getName(),
                    usage.getInit()/1024,
                    usage.getUsed()/1024,
                    usage.getCommitted()/1024,
                    usage.getMax()/1024);
            }
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                return;
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
