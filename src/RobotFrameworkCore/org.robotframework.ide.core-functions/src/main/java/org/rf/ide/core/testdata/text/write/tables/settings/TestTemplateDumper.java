/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.settings;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.table.RobotElementsComparatorWithPositionChangedPresave;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.TestTemplate;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.testdata.text.write.DumperHelper;
import org.rf.ide.core.testdata.text.write.tables.ANotExecutableTableElementDumper;

public class TestTemplateDumper extends ANotExecutableTableElementDumper<SettingTable> {

    public TestTemplateDumper(final DumperHelper helper) {
        super(helper, ModelType.SUITE_TEST_TEMPLATE);
    }

    @Override
    public RobotElementsComparatorWithPositionChangedPresave getSorter(
            final AModelElement<SettingTable> currentElement) {
        final TestTemplate testTemplate = (TestTemplate) currentElement;

        final RobotElementsComparatorWithPositionChangedPresave sorter = new RobotElementsComparatorWithPositionChangedPresave();
        final List<RobotToken> keys = new ArrayList<>();
        if (testTemplate.getKeywordName() != null) {
            keys.add(testTemplate.getKeywordName());
        }
        sorter.addPresaveSequenceForType(RobotTokenType.SETTING_TEST_TEMPLATE_KEYWORD_NAME, 1, keys);
        sorter.addPresaveSequenceForType(RobotTokenType.SETTING_TEST_TEMPLATE_KEYWORD_UNWANTED_ARGUMENT, 2,
                testTemplate.getUnexpectedArguments());
        sorter.addPresaveSequenceForType(RobotTokenType.COMMENT, 3,
                elemUtility.filter(testTemplate.getComment(), RobotTokenType.COMMENT));

        return sorter;
    }
}
