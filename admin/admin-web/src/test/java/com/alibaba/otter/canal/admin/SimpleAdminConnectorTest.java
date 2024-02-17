package com.alibaba.otter.canal.admin;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import org.junit.Ignore;
import org.junit.Test;

import com.alibaba.otter.canal.admin.connector.SimpleAdminConnector;

@Ignore
public class SimpleAdminConnectorTest {

    @Test
    public void testSimple() {
        System.out.println("task_" + NanoIdUtils.randomNanoId());
    }
}
