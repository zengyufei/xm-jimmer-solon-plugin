package org.babyfish.jimmer.sql.example.repository;

import com.xunmo.jimmer.repository.JRepository;
import org.babyfish.jimmer.sql.example.model.TreeNode;
import org.babyfish.jimmer.sql.fetcher.Fetcher;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface TreeNodeRepository extends JRepository<TreeNode, Long> {

    List<TreeNode> findByParentIsNullAndName(
            @Nullable String name,
            Fetcher<TreeNode> fetcher
    );
}
