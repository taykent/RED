/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.assist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robotframework.red.junit.jupiter.ProjectExtension.createFile;
import static org.robotframework.red.junit.jupiter.ProjectExtension.getFile;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.rf.ide.core.testdata.model.table.RobotEmptyRow;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.red.jface.assist.AssistantContext;
import org.robotframework.red.jface.assist.RedContentProposal;
import org.robotframework.red.junit.jupiter.FreshShell;
import org.robotframework.red.junit.jupiter.FreshShellExtension;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;
import org.robotframework.red.nattable.edit.AssistanceSupport.NatTableAssistantContext;

@ExtendWith({ ProjectExtension.class, FreshShellExtension.class })
public class TemplateArgumentsProposalsProviderTest {

    @Project
    static IProject project;

    @FreshShell
    Shell shell;

    private static RobotModel robotModel;

    @BeforeAll
    public static void beforeSuite() throws Exception {
        robotModel = RedPlugin.getModelManager().getModel();

        createFile(project, "res.robot",
                "*** Keywords ***",
                "Simple Keyword Name",
                "  [Arguments]  ${a1}  ${a2}  ${a3}",
                "  Log Many  ${a1}  ${a2}  ${a3}");

        createFile(project, "suite_no_template.robot",
                "*** Test Cases ***",
                "case",
                "  [Template]  NONE",
                "  [Setup]  ",
                "  ",
                "  abc",
                "  ",
                "  abc  def  ghi");

        createFile(project, "suite.robot",
                "*** Test Cases ***",
                "case",
                "  [Template]  Simple Keyword Name",
                "  ",
                "  abc",
                "  ",
                "  abc  def  ghi",
                "*** Settings ***",
                "Resource  res.robot");
    }

    @AfterAll
    public static void afterSuite() {
        RedPlugin.getModelManager().dispose();
    }

    @Test
    public void thereAreNoProposalsProvided_whenTemplateIsNotUsed() {
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(getFile(project, "suite_no_template.robot"));
        final List<RobotKeywordCall> calls = suiteFile.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren();

        final IRowDataProvider<Object> dataProvider = prepareDataProvider(calls);
        final TemplateArgumentsProposalsProvider provider = new TemplateArgumentsProposalsProvider(suiteFile,
                dataProvider);

        for (int column = 0; column < 10; column++) {
            for (int row = 0; row < calls.size(); row++) {
                final AssistantContext context = new NatTableAssistantContext(column, row);
                assertThat(provider.computeProposals("foo", 0, context)).isNull();
            }
        }
    }

    @Test
    public void thereAreProposalsProvidedOnlyInFirstColumnOfEmptyRow_whenTemplateIsUsed() {
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(getFile(project, "suite.robot"));
        final List<RobotKeywordCall> calls = suiteFile.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren();

        final IRowDataProvider<Object> dataProvider = prepareDataProvider(calls);
        final TemplateArgumentsProposalsProvider provider = new TemplateArgumentsProposalsProvider(suiteFile,
                dataProvider);

        for (int column = 0; column < 10; column++) {
            for (int row = 0; row < calls.size(); row++) {
                final AssistantContext context = new NatTableAssistantContext(column, row);
                if (column == 0 && calls.get(row).getLinkedElement() instanceof RobotEmptyRow) {
                    assertThat(provider.computeProposals("foo", 0, context)).isNotNull();
                } else {
                    assertThat(provider.computeProposals("foo", 0, context)).isNull();
                }
            }
        }
    }

    @Test
    public void thereAreProposalsProvided_andProperContentIsInserted() throws Exception {
        final Text text = new Text(shell, SWT.SINGLE);
        text.setText("foo");

        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(getFile(project, "suite.robot"));
        final List<RobotKeywordCall> calls = suiteFile.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren();

        final IRowDataProvider<Object> dataProvider = prepareDataProvider(calls);
        final TemplateArgumentsProposalsProvider provider = new TemplateArgumentsProposalsProvider(suiteFile,
                dataProvider);

        final AssistantContext context = new NatTableAssistantContext(0, 1);
        final RedContentProposal[] proposals = provider.computeProposals(text.getText(), 0, context);
        assertThat(proposals).hasSize(1);

        assertThat(proposals[0].getModificationStrategy().shouldSelectAllAfterInsert()).isTrue();
        assertThat(proposals[0].getModificationStrategy().shouldCommitAfterInsert()).isFalse();

        proposals[0].getModificationStrategy().insert(text, proposals[0]);
        assertThat(text.getText()).isEqualTo("a1");
    }

    @Test
    public void thereAreOperationsToPerformAfterAccepting_whenProposalHasArguments() throws Exception {
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(getFile(project, "suite.robot"));
        final List<RobotKeywordCall> calls = suiteFile.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren();

        final IRowDataProvider<Object> dataProvider = prepareDataProvider(calls);
        final TemplateArgumentsProposalsProvider provider = new TemplateArgumentsProposalsProvider(suiteFile,
                dataProvider);

        final AssistantContext context = new NatTableAssistantContext(0, 1);
        final RedContentProposal[] proposals = provider.computeProposals("foo", 0, context);
        assertThat(proposals).hasSize(1);

        assertThat(proposals[0].getLabel()).isEqualTo("Arguments for: Simple Keyword Name");
        assertThat(proposals[0].getOperationsToPerformAfterAccepting()).hasSize(1);
    }

    private static IRowDataProvider<Object> prepareDataProvider(final List<RobotKeywordCall> calls) {
        @SuppressWarnings("unchecked")
        final IRowDataProvider<Object> dataProvider = mock(IRowDataProvider.class);
        for (int i = 0; i < calls.size(); i++) {
            when(dataProvider.getRowObject(i)).thenReturn(calls.get(i));
        }
        return dataProvider;
    }

}
