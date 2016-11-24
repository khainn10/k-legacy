// Copyright (c) 2015-2016 K Team. All Rights Reserved.
package org.kframework.DebugMode;


import org.kframework.debugger.KDebug;
import org.kframework.kompile.CompiledDefinition;
import org.kframework.utils.errorsystem.KExceptionManager;
import org.kframework.utils.file.FileUtil;

/**
 * Created by manasvi on 7/23/15.
 */
public interface Command {

    public void runCommand(KDebug session, CompiledDefinition compiledDefinition, boolean disableOutput, FileUtil files, KExceptionManager kem);
}
