import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Fill in the implementation details of the class DecisionTree using this file. Any methods or
 * secondary classes that you want are fine but we will only interact with those methods in the
 * DecisionTree framework.
 * 
 * You must add code for the 1 member and 4 methods specified below.
 * 
 * See DecisionTree for a description of default methods.
 */
public class DecisionTreeImpl extends DecisionTree {
  private DecTreeNodeImpl root;
  //ordered list of class labels
  private List<String> labels; 
  //ordered list of attributes
  private List<String> attributes; 
  //map to ordered discrete values taken by attributes
  private Map<String, List<String>> attributeValues; 
  
  /**
   * Answers static questions about decision trees.
   */
  DecisionTreeImpl() {
    // no code necessary this is void purposefully
  }

  /**
   * Build a decision tree given only a training set.
   * 
   * @param train: the training set
   */
  DecisionTreeImpl(DataSet train) {
	// Initialize instance variables
    this.labels = train.labels;
    this.attributes = train.attributes;
    this.attributeValues = train.attributeValues;
    
    // Call recursive buildTree method to actually build the tree based on the training set
    root = buildTree(train.instances, attributes, null, null, "G");
  }

  @Override
  /**
   * Classify a given instance based on a previously built decision tree
   * 
   * @param instance: the instance to classify
   * 
   * @return the class/label of the instance predicted by the decision tree
   */
  public String classify(Instance instance) {
	  DecTreeNode currentNode = root;
	  
	  // While not a leaf node, plus sanity check to prevent crash
	  while ((currentNode != null) && (!currentNode.terminal))
	  {
		  // Attribute associated with the node
		  String attribute = currentNode.attribute;
		  
		  // Look up value in instance which corresponds to the node's attribute
		  int attributeIndex = getAttributeIndex(attribute);
		  String instanceValue = instance.attributes.get(attributeIndex);
		  
		  // Find index of instance value, so know which child node to visit
		  int valueIndex = getAttributeValueIndex(attribute, instanceValue);
		  currentNode = currentNode.children.get(valueIndex);
	  }
	  
	  // Return label predicted by tree (or default if something goes wrong)
	  if (currentNode != null)
		  return currentNode.label;
	  else
		  return labels.get(0);
  }

  @Override
  /**
   * Prints the Information Gain for each attribute for the root.
   * 
   * @param train: the training set
   */
  public void rootInfoGain(DataSet train) {
    this.labels = train.labels;
    this.attributes = train.attributes;
    this.attributeValues = train.attributeValues;
    
    // Use bestAttribute to print the gain at the root
    bestAttribute(train.instances, attributes, true);
  }
 
  @Override
  /**
   * Display the accuracy of the decision tree for a given test set.
   * 
   * @param test: the testing set
   */
  public void printAccuracy(DataSet test) {
	  List<Instance> testInstances = test.instances;
	  int numberCorrect = 0;
	  int total = testInstances.size();
	  double accuracy;
	  
	  for (Instance instance : testInstances)
	  {
		  String classification = classify(instance);
		  if (classification.equals(instance.label))
			  numberCorrect++;
	  }
	  accuracy = (double) numberCorrect / total;
	  System.out.format("%.5f\n", accuracy);
  }
  
  /**
   * Build a decision tree given a training set then prune it using a tuning set.
   * ONLY for extra credits
   * @param train: the training set
   * @param tune: the tuning set
   */
  DecisionTreeImpl(DataSet train, DataSet tune) {

    this.labels = train.labels;
    this.attributes = train.attributes;
    this.attributeValues = train.attributeValues;
    // TODO: add code here
    // only for extra credits
  }
  
  @Override
  /**
   * Print the decision tree in the specified format
   */
  public void print() {

    printTreeNode(root, null, 0);
  }

  /**
   * Prints the subtree of the node with each line prefixed by 4 * k spaces.
   */
  public void printTreeNode(DecTreeNode p, DecTreeNode parent, int k) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < k; i++) {
      sb.append("    ");
    }
    String value;
    if (parent == null) {
      value = "ROOT";
    } else {
      int attributeValueIndex = this.getAttributeValueIndex(parent.attribute, p.parentAttributeValue);
      value = attributeValues.get(parent.attribute).get(attributeValueIndex);
    }
    sb.append(value);
    if (p.terminal) {
      sb.append(" (" + p.label + ")");
      System.out.println(sb.toString());
    } else {
      sb.append(" {" + p.attribute + "?}");
      System.out.println(sb.toString());
      for (DecTreeNode child : p.children) {
        printTreeNode(child, p, k + 1);
      }
    }
  }
  
  /**
   * Actually builds a decision tree given a training set.
   * 
   * @param instances: the instances assigned to the node being built
   * @param attributes: the attributes uses in the training set
   * @param parentNode: the parent of the node we are currently building
   * @param defaultClass: the default label to use if instances is empty
   * 
   * @return the node that was built (and indirectly the subtree rooted at that node)
   */
  private DecTreeNodeImpl buildTree(List<Instance> instances, List<String> attributes, DecTreeNodeImpl parentNode, String parentAttributeValue, String defaultClass)
  {
	  String nodeAttribute;
	  List<String> nodeAttributes = new ArrayList<>(attributes);  // Holds the attributes still available for use
	  DecTreeNodeImpl node;
	  
	  // There are no more instances left; create leaf with default class
	  if (instances.isEmpty())
		  return new DecTreeNodeImpl(defaultClass, null, parentAttributeValue, true, null);
	  
	  // If all the instances have the same label, create a leaf with that label
	  if (sameLabel(instances))
	  {
		  return new DecTreeNodeImpl(instances.get(0).label, null, parentAttributeValue, true, instances);
	  }
	  
	  // If no more attributes left to assign to a node, make leaf based on majority vote
	  if (attributes.isEmpty())
	  {
		  return new DecTreeNodeImpl(majorityVote(instances), null, parentAttributeValue, true, instances);
	  }
	  
	  // Find best attribute to assign to the node
	  nodeAttribute = bestAttribute(instances, nodeAttributes, false);
	  nodeAttributes.remove(nodeAttribute);
	  if (parentNode == null)
		  node = new DecTreeNodeImpl(null, nodeAttribute, null, false, instances);  // root has no parent
	  else
		  node = new DecTreeNodeImpl(null, nodeAttribute, parentAttributeValue, false, instances);
	  
	  // Create child nodes, with the arcs being the values from the node's attribute
	  List<String> possibleValues = attributeValues.get(nodeAttribute);
	  for (String value : possibleValues)
	  {
		  List<Instance> childInstances = createSubset(instances, nodeAttribute, value);
		  DecTreeNodeImpl childNode;
		  childNode = buildTree(childInstances, nodeAttributes, node, value, majorityVote(instances));
		  node.children.add(childNode);
	  }
	  return node;
  }

  /**
   * Determines the majority label for a set of instances
   * 
   * @param instances: the instances to find the majority class for
   * 
   * @return the majority class
   */
  private String majorityVote(List<Instance> instances)
  {
	  String class1 = labels.get(0);
	  int class1Count = 0;
	  String class2 = labels.get(1);
	  int class2Count = 0;
	  for (Instance instance : instances)
	  {
		  if (instance.label.equals(class1))
			  class1Count++;
		  else
			  class2Count++;
	  }
	  if (class1Count >= class2Count)
		  return class1;
	  else
		  return class2;
  }
 
  /**
   * Determines if all the instances in a set have the same label/class
   * 
   * @param instances: the instances to check
   * 
   * @return true if they have the same label, else false
   */
  private boolean sameLabel(List<Instance> instances)
  {
	  String firstLabel = instances.get(0).label;
	  for (int i = 1; i < instances.size(); i++)
	  {
		  if (!instances.get(i).label.equals(firstLabel))
			  return false;
	  }
	  return true;
  }
  
  /**
   * Finds the best attribute to use at a node.
   * 
   * @param instances: the subset of examples available to that node
   * @param nodeAttributes: the attributes available to choose from
   * @param printGain: true if the method should print the gain associated with each attribute
   * 
   * @return the best attribute to use for the node
   */
  private String bestAttribute(List<Instance> instances, List<String> nodeAttributes, boolean printGain)
  {
	  // Initialize data structure that holds the totals used in information gain calculations
	  // the first dimension is the attribute, the second is for the attribute value
	  int class1Total = 0;
	  int class2Total = 0;
	  int[][] class1ValueCount = new int[attributes.size()][];  // Holds class counts for a given attribute and value
	  int[][] class2ValueCount = new int[attributes.size()][];
	  for (int a = 0; a < attributes.size(); a++)  // loops through attributes
	  {
		  String attribute = attributes.get(a);
		  List<String> values = attributeValues.get(attribute);
		  class1ValueCount[a] = new int[values.size()];
		  class2ValueCount[a] = new int[values.size()];
		  for (int v = 0; v < values.size(); v++)
		  {
			  class1ValueCount[a][v] = 0;  // Initialize to 0
			  class2ValueCount[a][v] = 0;
		  }
	  }
	  
	  // Calculate the totals used in the information gain calculations
	  for (Instance instance : instances)
	  {
		  String instanceLabel = instance.label;
		  int instanceLabelIndex = getLabelIndex(instanceLabel);
		  if (instanceLabelIndex == 0)
			  class1Total += 1;
		  else
			  class2Total +=1;
		  
		  List<String> instanceAttrValues = instance.attributes;
		  for (int x = 0; x < instanceAttrValues.size(); x++)
		  {
			  String instanceAttribute = attributes.get(x);
			  String instanceAttributeValue = instanceAttrValues.get(x);
			  int y = getAttributeValueIndex(instanceAttribute, instanceAttributeValue);
			  if (instanceLabelIndex == 0)
				  class1ValueCount[x][y] += 1;
			  else
				  class2ValueCount[x][y] += 1;
		  }
	  }
	  
	  // Calculate the information gain for each attribute
	  // First calculate the entropy
	  double prob1 = (double) class1Total/instances.size();
	  double prob2 = (double) class2Total/instances.size();
	  double entropy = -prob1 * log2(prob1) + -prob2 * log2(prob2);
	  // Now calculate the conditional entropies
	  String bestAttribute = null;
	  double bestInfoGain = -0.1;
	  
	  for (String attr : nodeAttributes)  // only consider attributes that have not been used
	  {
		  int a = getAttributeIndex(attr);
		  double conditionalEntropy = 0;
		  for (int v = 0; v < class1ValueCount[a].length; v++)
		  {
			  double specificCondEntropy;
			  double class1AAndVCount = class1ValueCount[a][v];
			  double class2AAndVCount = class2ValueCount[a][v];
			  double totalAAndVCount = class1AAndVCount + class2AAndVCount;
			  double probClass1;
			  double probClass2;
			  if (totalAAndVCount < 1)
			  {
				  probClass1 = 0;
				  probClass2 = 0;
			  }
			  else
			  {
				  probClass1 = class1AAndVCount/totalAAndVCount;
				  probClass2 = class2AAndVCount/totalAAndVCount;
			  }
			  specificCondEntropy = -(probClass1) * log2(probClass1) + -(probClass2) * log2(probClass2);
			  conditionalEntropy += ((totalAAndVCount/instances.size()) * specificCondEntropy);
		  }
		  double infoGain = entropy - conditionalEntropy;
		  if (printGain)
		  {
		      System.out.print(attr + " ");
		      System.out.format("%.5f\n", infoGain);
		  }
		  
		  if (infoGain > bestInfoGain)
		  {
			  bestInfoGain = infoGain;
			  bestAttribute = attr;
		  }
	  }
	  return bestAttribute;
  }
  
  /**
   * Creates a subset of a given set based on an attribute and value.
   * 
   * @param instances: the set to create the subset from
   * @param attribute: the attribute to use when creating the subset
   * @param value: the value of the given attribute members of the subset should have
   * 
   * @return a subset of instances
   */
  private List<Instance> createSubset(List<Instance> instances, String attribute, String value)
  {
	  List<Instance> subset = new ArrayList<>();
	  int attributeIndex = getAttributeIndex(attribute);
	  for (Instance instance : instances)
	  {
		  String instanceValue = instance.attributes.get(attributeIndex);
		  if (instanceValue.equals(value))
			  subset.add(instance);
	  }
	  return subset;
  }
  
  /**
   * Calculate log base 2.
   * 
   * @param arg: the value to take the log of
   * 
   * @return log base 2 of the argument
   */
  private double log2(double arg)
  {
	  if (arg == 0.0)
		  return 0.0;
	  else
		  return Math.log(arg)/Math.log(2);
  }

  /**
   * Helper function to get the index of the label in labels list
   */
  private int getLabelIndex(String label) {
    for (int i = 0; i < this.labels.size(); i++) {
      if (label.equals(this.labels.get(i))) {
        return i;
      }
    }
    return -1;
  }
 
  /**
   * Helper function to get the index of the attribute in attributes list
   */
  private int getAttributeIndex(String attr) {
    for (int i = 0; i < this.attributes.size(); i++) {
      if (attr.equals(this.attributes.get(i))) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Helper function to get the index of the attributeValue in the list for the attribute key in the attributeValues map
   */
  private int getAttributeValueIndex(String attr, String value) {
    for (int i = 0; i < attributeValues.get(attr).size(); i++) {
      if (value.equals(attributeValues.get(attr).get(i))) {
        return i;
      }
    }
    return -1;
  }
}
