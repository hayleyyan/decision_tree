import java.util.List;

/**
 * A subclass of DecTreeNode that also stores the instances associated with that node
 * and adds a helper function
 */
public class DecTreeNodeImpl extends DecTreeNode {
	// Instance Variables
	private List<Instance> nodeInstances;  // Stores subset of instances associated with the node

	// Constructor
	public DecTreeNodeImpl(String _label, String _attribute, String _parentAttributeValue, boolean _terminal, List<Instance> _nodeInstances) {
		super(_label, _attribute, _parentAttributeValue, _terminal);
		nodeInstances = _nodeInstances;
	}
	
	// Public Methods
	/**
	 * Determines if the given node is a leaf node
	 * 
	 * @return true if it is a leaf, else false
	 */
	public boolean isLeaf()
	{
		return terminal;
	}

}
