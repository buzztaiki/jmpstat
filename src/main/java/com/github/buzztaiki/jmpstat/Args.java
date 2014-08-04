package com.github.buzztaiki.jmpstat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Args {
    private final List<String> args;

    private Args(List<String> args) {
        this.args = args;
    }

    public static Args of(String[] args) {
        return new Args(new ArrayList<>(Arrays.asList(args)));
    }

    public int size() {
        return args.size();
    }

    public String get(int n) {
        return get(n, null);
    }

    public String get(int n, String fallback) {
        if (args.size() > n) {
            return args.get(n);
        }
        return fallback;
    }
}
