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
    private final MBeanServerConnection server;
    private final PrintStream out;

    public JmpStat(MBeanServerConnection server, PrintStream out) {
        this.server = server;
        this.out = out;
    }

    public boolean hasMemoryPool(Set<String> poolNames) throws IOException {
        for (MemoryPoolMXBean memoryPool : getMemoryPools()) {
            if (poolNames.contains(memoryPool.getName())) {
                return true;
            }
        }
        return false;
    }

    public void printAllPools() throws IOException {
        for (MemoryPoolMXBean memoryPool : getMemoryPools()) {
            out.format("%s\t%s%n", memoryPool.getName(), memoryPool.getUsage());
        }
    }

    public void pollLoop(Set<String> poolNames, long interval) throws IOException {
        out.println("pool_name\tinit\tused\tcommitted\tmax");
        for (;;) {
            for (MemoryPoolMXBean memoryPool : getMemoryPools()) {
                if (!poolNames.contains(memoryPool.getName())) {
                    continue;
                }
                MemoryUsage usage = memoryPool.getUsage();
                out.format("%s\t%d\t%d\t%d\t%d%n",
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

    private Iterable<MemoryPoolMXBean> getMemoryPools() throws IOException {
        return ManagementFactory.getPlatformMXBeans(server, MemoryPoolMXBean.class);
    }
}
