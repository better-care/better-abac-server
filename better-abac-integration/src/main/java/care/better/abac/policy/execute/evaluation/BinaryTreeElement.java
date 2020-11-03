package care.better.abac.policy.execute.evaluation;

/**
 * @author Andrej Dolenc
 */
public interface BinaryTreeElement<L extends BinaryTreeElement<L, R>, R extends BinaryTreeElement<L, R>> {
    L getLeftChild();

    void setLeftChild(L leftChild);

    R getRightChild();

    void setRightChild(R rightChild);
}
