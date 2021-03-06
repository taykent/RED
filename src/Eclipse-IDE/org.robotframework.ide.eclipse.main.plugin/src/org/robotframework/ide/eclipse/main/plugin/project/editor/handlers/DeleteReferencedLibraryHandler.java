/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.handlers;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.stream.Stream;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibraryArgumentsVariant;
import org.rf.ide.core.project.RobotProjectConfig.RemoteLocation;
import org.robotframework.ide.eclipse.main.plugin.project.RedProjectConfigEventData;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigEvents;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput;
import org.robotframework.ide.eclipse.main.plugin.project.editor.handlers.DeleteReferencedLibraryHandler.E4DeleteReferencedLibraryHandler;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.ReferencedLibrariesContentProvider.RemoteLibraryViewItem;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

/**
 * @author Michal Anglart
 *
 */
public class DeleteReferencedLibraryHandler extends DIParameterizedHandler<E4DeleteReferencedLibraryHandler> {

    public DeleteReferencedLibraryHandler() {
        super(E4DeleteReferencedLibraryHandler.class);
    }

    public static class E4DeleteReferencedLibraryHandler {

        @Execute
        public void deleteReferencedLibraries(@Named(Selections.SELECTION) final IStructuredSelection selection,
                final RedProjectEditorInput input, final IEventBroker eventBroker) {

            final List<RemoteLocation> remotesToRemove = Selections
                    .getOptionalFirstElement(selection, RemoteLibraryViewItem.class)
                    .map(RemoteLibraryViewItem::getLocations)
                    .orElseGet(() -> Selections.getElements(selection, RemoteLocation.class));
            final List<ReferencedLibrary> libsToRemove = Selections.getElements(selection, ReferencedLibrary.class);
            final List<ReferencedLibraryArgumentsVariant> variantsToRemove = Selections
                    .getElementsStream(selection, ReferencedLibraryArgumentsVariant.class)
                    .filter(variant -> !libsToRemove.contains(variant.getParent()))
                    .collect(toList());

            boolean removedLibs = false;
            boolean removedVariants = false;
            boolean removedRemotes = false;

            if (!libsToRemove.isEmpty()) {
                removedLibs = input.getProjectConfiguration().removeReferencedLibraries(libsToRemove);
            }
            if (!variantsToRemove.isEmpty()) {
                final Multimap<ReferencedLibrary, ReferencedLibraryArgumentsVariant> groupedVariants = Multimaps
                        .index(variantsToRemove, ReferencedLibraryArgumentsVariant::getParent);
                for (final ReferencedLibrary refLib : groupedVariants.keySet()) {
                    removedVariants |= refLib.removeArgumentsVariants(groupedVariants.get(refLib));
                }
            }
            if (!remotesToRemove.isEmpty()) {
                removedRemotes = input.getProjectConfiguration().removeRemoteLocations(remotesToRemove);
            }


            if (removedLibs) {
                eventBroker.send(RobotProjectConfigEvents.ROBOT_CONFIG_LIBRARY_ADDED_REMOVED,
                        new RedProjectConfigEventData<>(input.getFile(), libsToRemove));
                input.getRobotProject().unregisterWatchingOnReferencedLibraries(libsToRemove);
            }
            if (removedVariants || removedRemotes) {
                final List<Object> allRemoved = Stream.of(variantsToRemove, remotesToRemove)
                        .flatMap(List::stream)
                        .collect(toList());
                eventBroker.send(RobotProjectConfigEvents.ROBOT_CONFIG_LIBRARIES_ARGUMENTS_REMOVED,
                        new RedProjectConfigEventData<>(input.getFile(),
                                allRemoved));
            }
        }
    }
}
