package com.crudexample.democrud.ui;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import com.crudexample.democrud.model.User;
import com.crudexample.democrud.repo.UserRepository;
import com.vaadin.annotations.Theme;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.ValueChangeMode;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.dnd.DragSourceExtension;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
@SpringUI
@Theme("valo")
public class VaadinUI extends UI {

	private final UserRepository repo;
	
	private final UserEditor editor;

	final Grid<User> grid;

	final TextField filter;

	private final Button addNewBtn;

	@Autowired
	public VaadinUI(UserRepository repo, UserEditor editor) {
		this.repo = repo;
		this.editor = editor;
		this.grid = new Grid<>(User.class);
		this.filter = new TextField();
		this.addNewBtn = new Button("New user", VaadinIcons.PLUS);
	}

	@Override
	protected void init(VaadinRequest request) {
		HorizontalLayout actions = new HorizontalLayout(filter, addNewBtn);
		VerticalLayout mainLayout = new VerticalLayout(actions, grid, editor);
		setContent(mainLayout);
		addNewBtn.setStyleName(ValoTheme.BUTTON_FRIENDLY);

		grid.setHeight(400, Unit.PIXELS);
		grid.setWidth(800, Unit.PIXELS);
		grid.setColumns("id", "firstName", "lastName", "email","experience","birthDate");

		// Filtering
		filter.setPlaceholder("Filter by lastname");
		filter.setValueChangeMode(ValueChangeMode.LAZY);
		filter.addValueChangeListener(e -> listCustomers(e.getValue()));

		// selected user connection
		grid.asSingleSelect().addValueChangeListener(e -> editor.editUser(e.getValue()));

		// new user
		addNewBtn.addClickListener(e -> editor.editUser(new User(null, null)));

		editor.setChangeHandler(() -> {
			editor.setVisible(false);
			listCustomers(filter.getValue());
		});

		listCustomers(null);
	}

	void listCustomers(String filterText) {
		if (StringUtils.isEmpty(filterText)) {
			grid.setItems(repo.findAll());
		} else {
			grid.setItems(repo.findByLastNameStartsWithIgnoreCase(filterText));
		}
	}

}
