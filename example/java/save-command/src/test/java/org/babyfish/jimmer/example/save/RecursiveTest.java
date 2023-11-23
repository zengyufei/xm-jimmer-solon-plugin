package org.babyfish.jimmer.example.save;

import org.babyfish.jimmer.example.save.common.AbstractMutationTest;
import org.babyfish.jimmer.example.save.common.ExecutedStatement;
import org.babyfish.jimmer.example.save.model.TreeNodeDraft;
import org.babyfish.jimmer.example.save.model.TreeNodeProps;
import org.babyfish.jimmer.sql.DissociateAction;
import org.junit.jupiter.api.Test;

import java.util.Collections;

/**
 * Recommended learning sequence: 6
 *
 * <p>SaveModeTest -> IncompleteObjectTest -> ManyToOneTest ->
 * OneToManyTest -> ManyToManyTest -> [current: RecursiveTest] -> TriggerTest</p>
 */
public class RecursiveTest extends AbstractMutationTest {

    /*
     * Noun explanation
     *
     * Short Association: Association object(s) with only id property.
     * Long Association: Association object(s) with non-id properties.
     */

    @Test
    public void testCreateTree() {

        sql()
                .getEntities()
                .saveCommand(
                        /*
                         * `TreeNode` has two key properties: `name` and `parentNode`,
                         * this means `name` and `parentNode` must be specified when `id` is missing.
                         *
                         * One-to-many association is special, parent object can specify the
                         * many-to-one association of its child objects implicitly.
                         * In this demo, Associations named `childNodes` specify `parentNode`
                         * for child objects implicitly so that all child objects do not require
                         * the `parentNode`.
                         *
                         * However, the `parentNode` of ROOT cannot be specified implicitly,
                         * so that it must be specified manually
                         */
                        TreeNodeDraft.$.produce(root -> {
                            root.setParentNode(null);
                            root.setName("root");
                            root.addIntoChildNodes(child_1 -> {
                                child_1.setName("child-1");
                                child_1.addIntoChildNodes(child_1_1 -> {
                                    child_1_1.setName("child-1-1");
                                    child_1_1.setChildNodes(Collections.emptyList());
                                });
                                child_1.addIntoChildNodes(child_1_2 -> {
                                    child_1_2.setName("child-1-2");
                                    child_1_2.setChildNodes(Collections.emptyList());
                                });
                            });
                            root.addIntoChildNodes(child_2 -> {
                                child_2.setName("child-2");
                                child_2.addIntoChildNodes(child_2_1 -> {
                                    child_2_1.setName("child-2-1");
                                    child_2_1.setChildNodes(Collections.emptyList());
                                });
                                child_2.addIntoChildNodes(child_2_2 -> {
                                    child_2_2.setName("child-2-2");
                                    child_2_2.setChildNodes(Collections.emptyList());
                                });
                            });
                        })
                )
                /*
                 * You can also use `setAutoAttachingAll()`.
                 *
                 * If you use jimmer-spring-starter, it is unecessary to
                 * do it because this switch is turned on.
                 */
                .setAutoAttaching(TreeNodeProps.CHILD_NODES)
                .execute();

        assertExecutedStatements(

                // Query root
                new ExecutedStatement(
                        "select tb_1_.node_id, tb_1_.NAME, tb_1_.parent_id from TREE_NODE tb_1_ " +
                                "where tb_1_.NAME = ? and tb_1_.parent_id is null",
                        "root"
                ),

                // Root does not exist, insert it
                new ExecutedStatement(
                        "insert into TREE_NODE(NAME, parent_id) values(?, ?)",
                        "root", null
                ),

                // Query `child-1`
                new ExecutedStatement(
                        "select tb_1_.node_id, tb_1_.NAME, tb_1_.parent_id " +
                                "from TREE_NODE tb_1_ " +
                                "where tb_1_.NAME = ? and tb_1_.parent_id = ?",
                        "child-1", 1L
                ),

                // `child-1` does not exist, insert it
                new ExecutedStatement(
                        "insert into TREE_NODE(NAME, parent_id) values(?, ?)",
                        "child-1", 1L
                ),

                // Query `child-1-1`
                new ExecutedStatement(
                        "select tb_1_.node_id, tb_1_.NAME, tb_1_.parent_id " +
                                "from TREE_NODE tb_1_ " +
                                "where tb_1_.NAME = ? and tb_1_.parent_id = ?",
                        "child-1-1", 2L
                ),

                // `child-1-1` does not exist, insert it
                new ExecutedStatement(
                        "insert into TREE_NODE(NAME, parent_id) values(?, ?)",
                        "child-1-1", 2L
                ),

                // Query `child-1-2`
                new ExecutedStatement(
                        "select tb_1_.node_id, tb_1_.NAME, tb_1_.parent_id " +
                                "from TREE_NODE tb_1_ " +
                                "where tb_1_.NAME = ? and tb_1_.parent_id = ?",
                        "child-1-2", 2L
                ),

                // `child-1-2` does not exist, insert it
                new ExecutedStatement(
                        "insert into TREE_NODE(NAME, parent_id) values(?, ?)",
                        "child-1-2", 2L
                ),

                // Query `child-2`
                new ExecutedStatement(
                        "select tb_1_.node_id, tb_1_.NAME, tb_1_.parent_id " +
                                "from TREE_NODE tb_1_ " +
                                "where tb_1_.NAME = ? and tb_1_.parent_id = ?",
                        "child-2", 1L
                ),

                // `child-2` does not exist, insert it
                new ExecutedStatement(
                        "insert into TREE_NODE(NAME, parent_id) values(?, ?)",
                        "child-2", 1L
                ),

                // Query `child-2-1`
                new ExecutedStatement(
                        "select tb_1_.node_id, tb_1_.NAME, tb_1_.parent_id " +
                                "from TREE_NODE tb_1_ " +
                                "where tb_1_.NAME = ? and tb_1_.parent_id = ?",
                        "child-2-1", 5L
                ),

                // `child-2-1` does not exist, insert it
                new ExecutedStatement(
                        "insert into TREE_NODE(NAME, parent_id) values(?, ?)",
                        "child-2-1", 5L
                ),

                // Query `child-2-2`
                new ExecutedStatement(
                        "select tb_1_.node_id, tb_1_.NAME, tb_1_.parent_id " +
                                "from TREE_NODE tb_1_ " +
                                "where tb_1_.NAME = ? and tb_1_.parent_id = ?",
                        "child-2-2", 5L
                ),

                // `child-2-2` does not exist, insert it
                new ExecutedStatement(
                        "insert into TREE_NODE(NAME, parent_id) values(?, ?)",
                        "child-2-2", 5L
                )
        );
    }

    @Test
    public void testDeleteSubTrees() {
        jdbc(
                "insert into tree_node(node_id, name, parent_id) values" +
                        "(1, 'root', null)," +
                        "    (2, 'child-1', 1)," +
                        "        (3, 'child-1-1', 2)," +
                        "        (4, 'child-1-2', 2)," +
                        "    (5, 'child-2', 1)," +
                        "        (6, 'child-2-1', 5)," +
                        "        (7, 'child-2-2', 5)"
        );

        sql()
                .getEntities()
                .saveCommand(
                        // Please view the comment of `testCreateTree` to understand
                        // why `parentNode` is a key property of `TreeNode`
                        // but only the root node needs it.
                        TreeNodeDraft.$.produce(root -> {
                            root.setParentNode(null);
                            root.setName("root");
                            root.addIntoChildNodes(child_1 -> {
                                child_1.setName("child-1");
                                child_1.addIntoChildNodes(child_1_1 -> {
                                    child_1_1.setName("child-1-1");
                                    child_1_1.setChildNodes(Collections.emptyList());
                                });
                                // `child-1-2` in database will be deleted
                            });
                            // `-+-child-2`
                            // ` |`
                            // ` +----child-2-1`
                            // ` |`
                            // `-|----child-2-2`
                            // in database will be deleted
                        })
                )
                .setDissociateAction(TreeNodeProps.PARENT_NODE, DissociateAction.DELETE)
                .execute();

        assertExecutedStatements(

                // Query aggregate by key
                new ExecutedStatement(
                        "select tb_1_.node_id, tb_1_.NAME, tb_1_.parent_id " +
                                "from TREE_NODE tb_1_ " +
                                "where tb_1_.NAME = ? and tb_1_.parent_id is null",
                        "root"
                ),

                // Aggregate-root exists, but not changed, do nothing

                // Query `child-1` by key
                new ExecutedStatement(
                        "select tb_1_.node_id, tb_1_.NAME, tb_1_.parent_id " +
                                "from TREE_NODE tb_1_ " +
                                "where tb_1_.NAME = ? and tb_1_.parent_id = ?",
                        "child-1", 1L
                ),

                // `child-1` exists, but not changed, do nothing

                // Query `child-1-1` by key
                new ExecutedStatement(
                        "select tb_1_.node_id, tb_1_.NAME, tb_1_.parent_id " +
                                "from TREE_NODE tb_1_ " +
                                "where tb_1_.NAME = ? and tb_1_.parent_id = ?",
                        "child-1-1", 2L
                ),

                // `child-1-1` exists, but not changed, do nothing

                // Query child nodes of `child-1-1`
                new ExecutedStatement(
                        "select node_id from TREE_NODE where parent_id = ?",
                        3L
                ),

                // `child-1-1` does not have child nodes, do nothing

                // Query child nodes of `child-1` except `child-1-1`
                new ExecutedStatement(
                        "select node_id " +
                                "from TREE_NODE " +
                                "where parent_id = ? and node_id <> ?",
                        2L, 3L
                ),

                // Query child nodes of `child-1-2`
                new ExecutedStatement(
                        "select node_id from TREE_NODE where parent_id = ?",
                        4L
                ),

                // `child-1-2` does not have child nodes, do nothing

                // Delete `child-1-2`
                new ExecutedStatement(
                        "delete from TREE_NODE where node_id = ?",
                        4L
                ),

                // Query child nodes of root except `child-1`
                new ExecutedStatement(
                        "select node_id " +
                                "from TREE_NODE " +
                                "where parent_id = ? and node_id <> ?",
                        1L, 2L
                ),

                // Query child nodes of `child-2`
                new ExecutedStatement(
                        "select node_id from TREE_NODE where parent_id = ?",
                        5L
                ),

                // Query child nodes of `child-2-1` and `child-2-2`
                new ExecutedStatement(
                        "select node_id from TREE_NODE where parent_id in (?, ?)",
                        6L, 7L
                ),

                // `child-2-1` and `child-2-2` does not have child nodes, do nothing

                // Delete `child-2-1` and `child-2-2`
                new ExecutedStatement(
                        "delete from TREE_NODE where node_id in (?, ?)",
                        6L, 7L
                ),

                // Delete `child-2`
                new ExecutedStatement(
                        "delete from TREE_NODE where node_id = ?",
                        5L
                )
        );
    }
}
