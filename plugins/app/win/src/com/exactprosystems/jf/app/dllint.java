package com.company;

/**
 * Created by user_adm on 3/30/2016.
 */

import com.sun.jna.Library;

public interface dllint extends Library
{
    void run(String exec, String workDir, String param);
    void refresh();
    void stop();
}
