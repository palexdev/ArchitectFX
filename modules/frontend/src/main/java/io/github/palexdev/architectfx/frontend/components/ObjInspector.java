/*
 * Copyright (C) 2024 Parisi Alessandro - alessandro.parisi406@gmail.com
 * This file is part of ArchitectFX (https://github.com/palexdev/ArchitectFX)
 *
 * ArchitectFX is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * ArchitectFX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ArchitectFX. If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.palexdev.architectfx.frontend.components;


import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.function.Supplier;

import io.github.palexdev.architectfx.backend.model.UIObj;
import io.github.palexdev.architectfx.frontend.components.layout.Box;
import io.github.palexdev.architectfx.frontend.components.selection.SelectionModel;
import io.github.palexdev.architectfx.frontend.utils.ui.UIUtils;
import io.github.palexdev.imcache.utils.AsyncUtils;
import io.github.palexdev.mfxcomponents.controls.buttons.MFXIconButton;
import io.github.palexdev.mfxcomponents.controls.progress.MFXProgressIndicator;
import io.github.palexdev.mfxcomponents.controls.progress.ProgressDisplayMode;
import io.github.palexdev.mfxcomponents.theming.enums.PseudoClasses;
import io.github.palexdev.mfxcore.builders.bindings.BooleanBindingBuilder;
import io.github.palexdev.mfxcore.controls.Label;
import io.github.palexdev.mfxcore.controls.SkinBase;
import io.github.palexdev.mfxcore.controls.Text;
import io.github.palexdev.mfxcore.events.WhenEvent;
import io.github.palexdev.mfxcore.observables.When;
import io.github.palexdev.mfxcore.utils.converters.FunctionalStringConverter;
import io.github.palexdev.mfxcore.utils.fx.LayoutUtils;
import io.github.palexdev.mfxcore.utils.fx.TextUtils;
import io.github.palexdev.rectcut.Rect;
import io.github.palexdev.virtualizedfx.base.VFXContainer;
import io.github.palexdev.virtualizedfx.cells.CellBaseBehavior;
import io.github.palexdev.virtualizedfx.cells.VFXCellBase;
import io.github.palexdev.virtualizedfx.cells.base.VFXCell;
import io.github.palexdev.virtualizedfx.controls.VFXScrollPane;
import io.github.palexdev.virtualizedfx.events.VFXContainerEvent;
import io.github.palexdev.virtualizedfx.list.VFXList;
import io.github.palexdev.virtualizedfx.list.VFXListHelper;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Priority;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.util.StringConverter;

public class ObjInspector extends Box {
    //================================================================================
    // Properties
    //================================================================================
    private final ObjectProperty<UIObj> root = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            flattenTree();
        }
    };

    private final ObservableList<TreeItem> flattenedTree = FXCollections.observableArrayList();
    private final ReadOnlyBooleanWrapper updating = new ReadOnlyBooleanWrapper(false);
    private Future<?> updateTask;

    private boolean showDetails = false;
    private final Function<TreeItem, String> converter = t -> {
        if (t == null) return "null";
        UIObj obj = t.obj;
        return showDetails ?
            "%s@%s".formatted(obj.getType(), t.id) :
            "%s".formatted(obj.getType());
    };

    //================================================================================
    // Constructors
    //================================================================================
    public ObjInspector(Function<UIObj, Node> nodeResolver) {
        super(Direction.COLUMN);
        if (nodeResolver == null)
            throw new NullPointerException("Node resolver function cannot be null");

        /* Header */
        Text header = new Text("Inspector");
        header.getStyleClass().add("header");
        getContainerChildren().add(header);

        /* Tree View */
        SelectableVFXList<TreeItem, TreeItemCell> vfxList = new SelectableVFXList<>(
            flattenedTree,
            t -> new TreeItemCell(t)
                .setConverter(converter)
                .setNodeResolver(nodeResolver)
                .setShowDetailsChecker(() -> showDetails)
        );
        vfxList.setHelperFactory(o -> new TreeHelper(vfxList));
        vfxList.setFitToViewport(false);
        VFXScrollPane vsp = vfxList.makeScrollable();
        vsp.visibleProperty().bind(updatingProperty().not());
        setGrow(vsp, Priority.ALWAYS);
        getContainerChildren().add(vsp);

        SelectionModel<TreeItem> sm = vfxList.getSelectionModel();
        sm.setAllowsMultipleSelection(false);
        sm.selection().addListener((InvalidationListener) i -> {
            UIObj obj = sm.getSelectedItemOpt().map(TreeItem::obj).orElse(null);
            Node node = nodeResolver.apply(obj);
            fireEvent(new InspectorEvents(InspectorEvents.SHOW_BOUNDS_OVERLAY, node));
        });

        /* Actions */
        MFXIconButton dtsBtn = new MFXIconButton().asToggle();
        dtsBtn.getStyleClass().add("ids");
        dtsBtn.setOnAction(e -> {
            showDetails = dtsBtn.isSelected();
            vfxList.update();
        });
        dtsBtn.setSelected(showDetails);
        UIUtils.installTooltip(dtsBtn, "Show/Hide Details");

        MFXIconButton dtspBtn = new MFXIconButton();
        /* TODO implement */
        dtspBtn.disableProperty().bind(sm.selection().emptyProperty());
        dtspBtn.getStyleClass().add("details");
        UIUtils.installTooltip(dtspBtn, "Deep-inspect UIObj");

        Box actions = new Box(
            Direction.ROW,
            dtsBtn,
            dtspBtn
        ).addStyleClass("actions");
        actions.setMaxWidth(USE_PREF_SIZE);
        actions.visibleProperty().bind(updatingProperty().not());
        getContainerChildren().add(actions);

        /* Flattening progress indicator */
        MFXProgressIndicator indicator = new MFXProgressIndicator();
        indicator.setDisplayMode(ProgressDisplayMode.CIRCULAR);
        indicator.visibleProperty().bind(updatingProperty());
        indicator.setManaged(false);
        getContainerChildren().add(indicator);

        getStyleClass().add("inspector");
        /* TODO debugging */
        UIUtils.debugTheme(this, "css/components/ObjInspector.css");
    }

    //================================================================================
    // Methods
    //================================================================================

    protected void flattenTree() {
        setUpdating(true);
        if (updateTask != null) {
            updateTask.cancel(true);
        }

        flattenedTree.clear();

        UIObj root = getRoot();
        if (root == null) {
            setUpdating(false);
            return;
        }

        updateTask = AsyncUtils.runAsync(() -> {
            List<TreeItem> res = new ArrayList<>();
            Stack<TreeItem> stack = new Stack<>();
            stack.push(new TreeItem(null, root, 0));

            while (!stack.isEmpty()) {
                TreeItem item = stack.pop();
                UIObj curr = item.obj();
                int depth = item.depth();

                res.add(item);
                List<UIObj> children = curr.getChildren();
                for (int i = children.size() - 1; i >= 0; i--) {
                    stack.push(new TreeItem(item, children.get(i), depth + 1));
                }
            }

            Platform.runLater(() -> {
                flattenedTree.setAll(res);
                setUpdating(false);
            });
        });
    }

    //================================================================================
    // Overridden Methods
    //================================================================================

    @Override
    protected void layoutChildren() {
        super.layoutChildren();

        Node indicator = getContainerChildren().getLast();
        indicator.autosize();
        positionInArea(
            indicator,
            0, 0, getWidth(), getHeight(), 0,
            HPos.CENTER, VPos.CENTER
        );
    }

    //================================================================================
    // Getters/Setters
    //================================================================================
    public UIObj getRoot() {
        return root.get();
    }

    public ObjectProperty<UIObj> rootProperty() {
        return root;
    }

    public void setRoot(UIObj root) {
        this.root.set(root);
    }

    public boolean isUpdating() {
        return updating.get();
    }

    public ReadOnlyBooleanProperty updatingProperty() {
        return updating.getReadOnlyProperty();
    }

    protected void setUpdating(boolean updating) {
        this.updating.set(updating);
    }

    //================================================================================
    // Inner Classes
    //================================================================================
    protected static class TreeHelper extends VFXListHelper.VerticalHelper<TreeItem, TreeItemCell> {
        public static final double xOffset = 16.0;

        public TreeHelper(VFXList<TreeItem, TreeItemCell> list) {
            super(list);
        }

        @Override
        public void layout(int layoutIndex, VFXCell<TreeItem> cell) {
            if (cell instanceof TreeItemCell tic) {
                Node node = cell.toNode();
                int depth = Optional.ofNullable(tic.getItem())
                    .map(TreeItem::depth)
                    .orElse(0);
                double x = xOffset * depth;
                double y = getTotalCellSize() * layoutIndex;
                double w = computeSize(node);
                double h = list.getCellSize();
                cell.beforeLayout();
                node.resizeRelocate(x, y, w, h);
                cell.afterLayout();

                double totalW = w + x;
                if (totalW > getVirtualMaxX()) virtualMaxX.set(totalW);
            }
        }
    }

    public record TreeItem(long id, TreeItem parent, UIObj obj, int depth) {
        private static final SecureRandom ID_GENERATOR = new SecureRandom();

        public TreeItem(TreeItem parent, UIObj obj, int depth) {
            this(ID_GENERATOR.nextLong(1_000_000), parent, obj, depth);
        }
    }

    public static class TreeItemCell extends VFXCellBase<TreeItem> {
        private final ReadOnlyBooleanWrapper selected = new ReadOnlyBooleanWrapper() {
            @Override
            protected void invalidated() {
                PseudoClasses.SELECTED.setOn(TreeItemCell.this, get());
            }
        };
        private StringConverter<TreeItem> converter;
        private Function<UIObj, Node> nodeResolver;
        private Supplier<Boolean> showDetailsChecker;

        public TreeItemCell(TreeItem item) {
            super(item);
            setConverter(Objects::toString);
        }

        @Override
        public void onCreated(VFXContainer<TreeItem> container) {
            super.onCreated(container);
            if (container instanceof SelectableVFXList<TreeItem, ?> sList) {
                SelectionModel<TreeItem> sm = sList.getSelectionModel();
                selected.bind(BooleanBindingBuilder.build()
                    .setMapper(() -> sm.contains(getIndex()))
                    .addSources(sm.selection(), indexProperty())
                    .get()
                );
            }
        }

        @Override
        protected SkinBase<?, ?> buildSkin() {
            return new TreeItemCellSkin(this);
        }

        @Override
        public void dispose() {
            selected.unbind();
            super.dispose();
        }

        public boolean isSelected() {
            return selected.get();
        }

        public ReadOnlyBooleanProperty selectedProperty() {
            return selected.getReadOnlyProperty();
        }

        protected void setSelected(boolean selected) {
            this.selected.set(selected);
        }

        public StringConverter<TreeItem> getConverter() {
            return converter;
        }

        public TreeItemCell setConverter(StringConverter<TreeItem> converter) {
            this.converter = converter;
            return this;
        }

        public TreeItemCell setConverter(Function<TreeItem, String> fn) {
            return setConverter(FunctionalStringConverter.to(fn));
        }

        public Function<UIObj, Node> getNodeResolver() {
            return nodeResolver;
        }

        public TreeItemCell setNodeResolver(Function<UIObj, Node> nodeResolver) {
            this.nodeResolver = nodeResolver;
            return this;
        }

        public boolean isShowDetails() {
            return Optional.ofNullable(showDetailsChecker)
                .map(Supplier::get)
                .orElse(false);
        }

        public TreeItemCell setShowDetailsChecker(Supplier<Boolean> checker) {
            this.showDetailsChecker = checker;
            return this;
        }
    }

    private static class TreeItemCellSkin extends SkinBase<VFXCellBase<TreeItem>, CellBaseBehavior<TreeItem>> {
        private final Label typeLabel;
        private final Label classesLabel;
        private final Path line;

        private final StringProperty typeText = new SimpleStringProperty("");
        private final StringProperty classesText = new SimpleStringProperty("");

        public TreeItemCellSkin(TreeItemCell cell) {
            super(cell);
            cell.setPickOnBounds(false);

            typeLabel = new Label();
            typeLabel.textProperty().bind(typeText);
            typeLabel.getStyleClass().add("type");

            classesLabel = new Label();
            classesLabel.textProperty().bind(classesText);
            classesLabel.getStyleClass().add("classes");

            line = new Path();
            line.setManaged(false);
            line.setMouseTransparent(true);

            // Finalize init
            addListeners();
            getChildren().addAll(line, typeLabel, classesLabel);
        }

        protected void addListeners() {
            TreeItemCell cell = getCell();
            listeners(
                When.onInvalidated(cell.itemProperty())
                    .then(v -> update())
                    .executeNow(),
                When.onInvalidated(line.strokeWidthProperty())
                    .then(w -> drawConnector())
                    .executeNow()
            );
        }

        protected void drawConnector() {
            TreeItemCell cell = getCell();
            VFXList<?, ?> list = (VFXList<?, ?>) cell.getContainer();
            TreeItem item = cell.getItem();
            if (list == null || item == null || item.parent() == null) {
                line.setVisible(false);
                return;
            }

            int depth = Math.max(1, item.depth() - 1);
            double endX = -(depth * TreeHelper.xOffset - line.getStrokeWidth() / 2.0);
            double endY = -list.getHelper().getTotalCellSize();

            ObservableList<PathElement> elements = line.getElements();
            elements.clear();
            elements.add(new MoveTo(0, 0));
            elements.add(new LineTo(endX, 0.0)); // Horizontal line
            elements.add(new LineTo(endX, endY));   // Vertical line
            cell.requestLayout();
        }

        protected void update() {
            TreeItemCell cell = getCell();
            TreeItem item = cell.getItem();
            String typeText = cell.getConverter().toString(item);
            String classesText = Optional.ofNullable(cell.getNodeResolver().apply(item.obj()))
                .filter(n -> cell.isShowDetails() && !n.getStyleClass().isEmpty())
                .map(n -> Arrays.toString(n.getStyleClass().toArray()))
                .orElse("");
            this.typeText.set(typeText);
            this.classesText.set(classesText);
            drawConnector();
        }

        protected double labelsWidth() {
            double typeW = typeLabel.snappedLeftInset() +
                           TextUtils.computeTextWidth(typeLabel.getFont(), typeText.get()) +
                           typeLabel.snappedRightInset();
            double classesW = classesLabel.snappedLeftInset() +
                              TextUtils.computeTextWidth(classesLabel.getFont(), classesText.get()) +
                              classesLabel.snappedRightInset();
            return Math.max(typeW, classesW);
        }

        protected TreeItemCell getCell() {
            return ((TreeItemCell) getSkinnable());
        }

        @Override
        protected void initBehavior(CellBaseBehavior<TreeItem> behavior) {
            behavior.init();
            TreeItemCell cell = getCell();
            events(
                WhenEvent.intercept(cell, MouseEvent.MOUSE_CLICKED)
                    .process(e -> {
                        if (e.getButton() == MouseButton.SECONDARY) return;
                        VFXContainer<TreeItem> container = cell.getContainer();
                        if (container instanceof SelectableVFXList<TreeItem, ?> sList) {
                            SelectionModel<TreeItem> sm = sList.getSelectionModel();
                            int index = cell.getIndex();
                            boolean selected = sm.contains(index);
                            if (selected) {
                                sm.deselectIndex(index);
                            } else {
                                sm.selectIndex(index);
                            }
                        }
                    }),
                WhenEvent.intercept(cell, VFXContainerEvent.UPDATE)
                    .process(e -> update())
            );
        }

        @Override
        protected double computeMinWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
            return snappedLeftInset() + 240.0 + snappedRightInset();
        }

        @Override
        protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
            return snappedLeftInset() + labelsWidth() + snappedRightInset();
        }

        @Override
        protected void layoutChildren(double x, double y, double w, double h) {
            Rect area = Rect.of(x, y, w, h)
                .withInsets(new double[] {
                    snappedTopInset(),
                    snappedRightInset(),
                    snappedBottomInset(),
                    snappedLeftInset()
                });

            if (classesText.get().isEmpty()) {
                classesLabel.setVisible(false);
                area.layout(typeLabel::resizeRelocate);
            } else {
                area.cutTop(LayoutUtils.snappedBoundHeight(typeLabel))
                    .layout(typeLabel::resizeRelocate);
                area.cutBottom(LayoutUtils.snappedBoundHeight(classesLabel))
                    .layout(classesLabel::resizeRelocate);
                classesLabel.setVisible(true);
            }

            line.setLayoutY((h - line.getStrokeWidth()) / 2.0);
        }
    }

    public static class InspectorEvents extends Event {

        public static final EventType<InspectorEvents> ANY = new EventType<>("INSPECTOR_EVENT");
        public static final EventType<InspectorEvents> SHOW_BOUNDS_OVERLAY = new EventType<>("SHOW_BOUNDS_OVERLAY");

        private final Node node;

        public InspectorEvents(EventType<InspectorEvents> eventType, Node node) {
            super(eventType);
            this.node = node;
        }

        public Node getNode() {
            return node;
        }
    }
}
