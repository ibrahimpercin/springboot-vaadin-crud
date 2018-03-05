package com.crudexample.democrud.ui;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import com.crudexample.democrud.model.Exp;
import com.crudexample.democrud.model.User;
import com.crudexample.democrud.repo.UserRepository;
import com.vaadin.annotations.Theme;
import com.vaadin.data.Binder;
import com.vaadin.data.Binder.Binding;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.ValueChangeMode;
import com.vaadin.shared.ui.dnd.DropEffect;
import com.vaadin.shared.ui.dnd.EffectAllowed;
import com.vaadin.shared.ui.grid.DropLocation;
import com.vaadin.shared.ui.grid.DropMode;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.components.grid.GridDragSource;
import com.vaadin.ui.components.grid.GridDropTarget;
import com.vaadin.ui.components.grid.MultiSelectionModel;
import com.vaadin.ui.dnd.DragSourceExtension;
import com.vaadin.ui.dnd.DropTargetExtension;
import com.vaadin.ui.renderers.ComponentRenderer;
import com.vaadin.ui.themes.ValoTheme;


@SuppressWarnings("serial")
@SpringUI
@Theme("valo")
public class VaadinUI extends UI {

	private final UserRepository repo;

	private final UserEditor editor;

	private User user;

	final Grid<User> gridLeft;

	final Grid<User> gridRight;

	final TextField filter;

	private final Button addNewBtn;

	private final Button deleteBtn;

	private List<User> draggedItems = null;

	private Set<User> deletedItems;

	@Autowired
	public VaadinUI(UserRepository repo, UserEditor editor) {
		this.repo = repo;
		this.editor = editor;
		this.gridLeft = new Grid<>(User.class);
		this.gridRight = new Grid<>(User.class);
		this.filter = new TextField();
		this.addNewBtn = new Button("New User", VaadinIcons.PLUS);
		this.deleteBtn = new Button("Delete User", VaadinIcons.TRASH);
	}

	@Override
	protected void init(VaadinRequest request) {
		/*
		 * Layouts
		 */
		createUIwithLayouts();
		
		// Grids
		dragnDropGridConfig();

		/*
		 * inline editing 
		 */
		inLineEdit(gridLeft);

		// Filtering
		filterByLastName();
				
		//Drag and change Value of grid row
		dragAndChangeValue();
		
		/*
		Window sub = new Window("I'm Modal");
		sub.setContent(new Label("Here's some content"));
		sub.setModal(true);
		UI.getCurrent().addWindow(sub);
		*/

		// Add new user
		addNewBtn.addClickListener(e -> editor.editUser(new User(null, null)));

		// Delete selected users
		deleteBtn.addClickListener(e -> {
			deletedItems = gridLeft.getSelectedItems();
			repo.deleteInBatch(deletedItems);
			listUsers(null,repo,gridLeft);
		});

		editor.setChangeHandler(() -> {
			editor.setVisible(false);
			listUsers(filter.getValue(), repo, gridLeft);
		});

		listUsers(null,repo,gridLeft);
		
		
	}

	@SuppressWarnings({ "unused", "unchecked" })
	public void dragnDropGridConfig() {
		// Grid selection
		MultiSelectionModel<User> leftSelectionModel = (MultiSelectionModel<User>) gridLeft
				.setSelectionMode(SelectionMode.MULTI);
		MultiSelectionModel<User> rightSelectionModel = (MultiSelectionModel<User>) gridRight
				.setSelectionMode(SelectionMode.MULTI);
		
/*	     // enable row dnd from left to right and handle drops
        GridRowDragger<User> leftToRight = new GridRowDragger<>(gridLeft, gridRight);
 
        // enable row dnd from right to left and handle drops
        GridRowDragger<User> rightToLeft = new GridRowDragger<>(gridRight, gridLeft);*/
		
		GridDragSource<User> dragSource = new GridDragSource<>(gridLeft);
		dragSource.setEffectAllowed(EffectAllowed.MOVE);
	
		dragSource.setDragDataGenerator("text", users -> {
			return users.getId() + " " + users.getFirstName() + " " + users.getLastName() + " " + users.getEmail() + " "
					+ users.getExperience() + " " + users.getBirthDate();
		});
	
		dragSource.addGridDragStartListener(event ->{
			//gridLeft.getBeanType().cast(user);
			draggedItems = event.getDraggedItems();
		});
	
		dragSource.addGridDragEndListener(event -> {
			// If drop was successful, remove dragged items from source Grid
			if (event.getDropEffect() == DropEffect.MOVE && draggedItems != null) {
				((ListDataProvider<User>) gridLeft.getDataProvider()).getItems().removeAll(draggedItems);
				gridLeft.getDataProvider().refreshAll();
	
				// Remove dragged items
				draggedItems = null;
			}
		});
	
		// grid as drop target
		GridDropTarget<User> dropTarget = new GridDropTarget<>(gridRight, DropMode.ON_GRID);
		dropTarget.setDropEffect(DropEffect.MOVE);
	
		dropTarget.addGridDropListener(event -> {
			// Accepting dragged items from another Grid in the same UI
			event.getDragSourceExtension().ifPresent(source -> {
				if (source instanceof GridDragSource) {
					// Get the target Grid's items
					ListDataProvider<User> dataProvider = (ListDataProvider<User>) gridLeft.getDataProvider();
					List<User> items = (List<User>) dataProvider.getItems();
	
					// Calculate the target row's index
					int index = items.size();
					if (event.getDropTargetRow().isPresent()) {
						index = items.indexOf(event.getDropTargetRow().get())
								+ (event.getDropLocation() == DropLocation.BELOW ? 1 : 0);
					}
	
					// Add dragged items to the target Grid
					items.addAll(index, draggedItems);
	
					dataProvider.refreshAll();
					gridRight.setItems(draggedItems);
					repo.delete(draggedItems);
					listUsers(null,repo,gridLeft);	
					
				}
			});
		});
	
		
	}

	public void filterByLastName() {
		filter.setPlaceholder("Filter by lastname");
		filter.setValueChangeMode(ValueChangeMode.LAZY);
		filter.addValueChangeListener(e -> listUsers(e.getValue(),repo,gridLeft));
	}
	
	public void inLineEdit(Grid<User> grid) {
		TextField firstNameTextField = new TextField();
		firstNameTextField.focus();
		TextField lastNameTextField = new TextField();
		TextField emailTextField = new TextField();
		ComboBox<Exp> experienceSelect = new ComboBox<>();
		experienceSelect.setItems(Exp.values());
		DateField birthDate = new DateField();
	
		Binder<User> inline = grid.getEditor().getBinder();
	
		// firstName in line with requiring
		Binder.Binding<User, String> firstNameBinder = inline.forField(firstNameTextField)
				.asRequired("First name cannot be empty")
				.bind(User::getFirstName, User::setFirstName);
	
		grid.getColumn("firstName").setEditorBinding(firstNameBinder);
	
		// lastName in line with requiring
		Binder.Binding<User, String> lastNameBinder = inline.forField(lastNameTextField)
				.asRequired("Last name cannot be empty")
				.bind(User::getLastName, User::setLastName);
	
		grid.getColumn("lastName").setEditorBinding(lastNameBinder);
	
		// email in line
		Binder.Binding<User, String> emailBinder = inline.forField(emailTextField)
				.bind(User::getEmail, User::setEmail);
	
		grid.getColumn("email").setEditorBinding(emailBinder);
	
		// experience in line
		Binding<User, Exp> experienceBinder = inline.forField(experienceSelect)
				.bind(User::getExperience, User::setExperience);
	
		grid.getColumn("experience").setEditorBinding(experienceBinder);
	
		// birthDate in line
		Binding<User, LocalDate> birthDateBinder = inline.forField(birthDate)
				.bind(User::getBirthDate, User::setBirthDate);
	
		grid.getColumn("birthDate").setEditorBinding(birthDateBinder);
	
		grid.getEditor().setEnabled(true);
		inline.readBean(user);
		grid.setItems(user);
		
	}

	@SuppressWarnings("unchecked")
	public void dragAndChangeValue() {
		
		DropTargetExtension<Grid<User>> targetRow = new DropTargetExtension<>(gridLeft);
		
		targetRow.addDropListener(event -> {
			Optional<AbstractComponent> dragSource = event.getDragSourceComponent();
			if (dragSource.isPresent() && dragSource.get() instanceof Label) {
		        // move the label to the layout
				ListDataProvider<User> dataProvider = (ListDataProvider<User>) gridLeft.getDataProvider();
				List<User> items = (List<User>) dataProvider.getItems();
				dataProvider.refreshAll();
				Set<User> selected = gridLeft.asMultiSelect().getValue();
				List<User> itemsList = selected.stream().collect(Collectors.toList());

				for (int i = 0; i < itemsList.size(); i++) {
					itemsList.get(i).setFirstName(event.getDataTransferText());
				}
				Notification.show("First name changed with dragged value", 
						Notification.Type.TRAY_NOTIFICATION);
				gridLeft.setItems(items);

		    }
		});
	}
	
	/*
	 * Layouts
	 */
	public void createUIwithLayouts() {

		Label dragLabel = new Label("Drag it and change first name");
		dragLabel.setStyleName(ValoTheme.LABEL_BOLD);
		DragSourceExtension<Label> dragSource = new DragSourceExtension<>(dragLabel);
		dragSource.setEffectAllowed(EffectAllowed.MOVE);
		dragSource.setDataTransferText("dragged value");

		// ---------------------------------------------------------
		
		HorizontalLayout actions = new HorizontalLayout(filter, addNewBtn, deleteBtn, dragLabel);
		HorizontalLayout secondLayout = new HorizontalLayout(gridLeft, gridRight);
		VerticalLayout mainLayout = new VerticalLayout(actions, secondLayout, editor);
		secondLayout.setSizeFull();
		setContent(mainLayout);
		addNewBtn.setStyleName(ValoTheme.BUTTON_FRIENDLY);
		deleteBtn.setStyleName(ValoTheme.BUTTON_DANGER);

		gridLeft.setHeight(50, Unit.PERCENTAGE);
		gridRight.setHeight(50, Unit.PERCENTAGE);

		gridLeft.setWidth(100, Unit.PERCENTAGE);
		gridRight.setWidth(100, Unit.PERCENTAGE);

		gridLeft.setColumns("id", "firstName", "lastName", "email", "experience", "birthDate");
		gridRight.setColumns("id", "firstName", "lastName", "email", "experience", "birthDate");
		
		// -----------  Draggable Row for grid -------------
		
		Map<User, TextField> textFields = new HashMap<>();
		gridLeft.addColumn(user -> {
		      // Check for existing text field
		      if (textFields.containsKey(user)) {
		            return textFields.get(user);
		      }
		      // Create a new one
		      TextField textField = new TextField();
		      textField.setStyleName(ValoTheme.TEXTFIELD_BORDERLESS);
		      textField.setValue(user.getFirstName());
		      DropTargetExtension<TextField> targetText = new DropTargetExtension<>(textField);
		      //Drop
		      targetText.addDropListener(event -> {
					Optional<AbstractComponent> dragSource2 = event.getDragSourceComponent();
					if (dragSource2.isPresent() && dragSource2.get() instanceof Label) {
				        // move the label to the layout

				      	Notification.show(textField.getValue() + "'s name changed with dragged value",
				                  Notification.Type.TRAY_NOTIFICATION);
						textField.setValue(event.getDataTransferText());
				    }
				});
		      // Store the text field when user updates the value
		      textField.addValueChangeListener(change ->
		            textFields.put(user, textField));
		      return textField;
		      }, new ComponentRenderer());

	}

	void listUsers(String filterText, UserRepository repo, Grid<User> grid) {
		if (StringUtils.isEmpty(filterText)) {
			grid.setItems(repo.findAll());
		} else {
			grid.setItems(repo.findByLastNameStartsWithIgnoreCase(filterText));
		}
	}

}
