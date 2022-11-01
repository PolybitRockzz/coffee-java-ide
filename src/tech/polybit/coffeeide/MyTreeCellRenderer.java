package tech.polybit.coffeeide;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

class MyTreeCellRenderer extends DefaultTreeCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3370059376211372147L;

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row,
				hasFocus);
		if (value instanceof DefaultMutableTreeNode) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
			if (node.getUserObject() instanceof String) {
				setIcon(UIManager.getIcon("FileView.computerIcon"));
			} else if (node.getUserObject() instanceof Contact) {
				Contact contact = (Contact) node.getUserObject();
				if (contact.isSomeProperty()) {
					setIcon(UIManager.getIcon("FileView.fileIcon"));
				} else {
					setIcon(UIManager.getIcon("FileView.directoryIcon"));
				}
			}
		}
		return this;
	}
}