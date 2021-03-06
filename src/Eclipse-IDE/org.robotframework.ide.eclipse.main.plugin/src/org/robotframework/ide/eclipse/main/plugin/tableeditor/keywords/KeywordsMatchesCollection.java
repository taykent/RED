/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords;

import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.CodeElementsMatchesCollection;

class KeywordsMatchesCollection extends CodeElementsMatchesCollection {

    @Override
    protected boolean shouldMatchLabel(final RobotKeywordCall call) {
        return !call.isArgumentsSetting();
    }
}
