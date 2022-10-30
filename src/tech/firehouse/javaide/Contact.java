package tech.firehouse.javaide;

class Contact {
	private boolean someProperty;
	private String name;

	public Contact(String name) {
		this(name, false);
	}

	public Contact(String name, boolean property) {
		this.someProperty = property;
		this.name = name;
	}

	public boolean isSomeProperty() {
		return someProperty;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}
}