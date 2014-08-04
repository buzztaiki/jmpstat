package com.github.buzztaiki.jmpstat;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.MemoryUsage;
import java.util.Set;
import javax.management.MBeanServerConnection;

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
        out.println("uptime\tpool_name\tinit\tused\tcommitted\tmax");
        RuntimeMXBean runtime = getRuntime();
        Iterable<MemoryPoolMXBean> memoryPools = getMemoryPools();
        for (;;) {
            for (MemoryPoolMXBean memoryPool : memoryPools) {
                if (!poolNames.contains(memoryPool.getName())) {
                    continue;
                }
                MemoryUsage usage = memoryPool.getUsage();
                out.format("%.1f\t%s\t%d\t%d\t%d\t%d%n",
                    runtime.getUptime()/1000.0,
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

    private RuntimeMXBean getRuntime() throws IOException {
        return ManagementFactory.getPlatformMXBean(server, RuntimeMXBean.class);
    }
}
