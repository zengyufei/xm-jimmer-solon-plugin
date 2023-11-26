package org.babyfish.jimmer.sql.example.service;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.babyfish.jimmer.client.FetchBy;
import org.babyfish.jimmer.client.ThrowsAll;
import org.babyfish.jimmer.spring.core.annotation.Db;
import org.babyfish.jimmer.sql.example.model.TreeNode;
import org.babyfish.jimmer.sql.example.model.TreeNodeDraft;
import org.babyfish.jimmer.sql.example.model.TreeNodeFetcher;
import org.babyfish.jimmer.sql.example.repository.TreeNodeRepository;
import org.babyfish.jimmer.sql.example.service.dto.FlatTreeNodeView;
import org.babyfish.jimmer.sql.example.service.dto.RecursiveTreeInput;
import org.babyfish.jimmer.sql.fetcher.Fetcher;
import org.babyfish.jimmer.sql.fetcher.RecursiveListFieldConfig;
import org.babyfish.jimmer.sql.runtime.SaveErrorCode;
import org.noear.solon.annotation.*;
import org.noear.solon.validation.annotation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/*
 * Why add spring web annotations to the service class?
 *
 * The success and popularity of rich client technologies represented by React, Vue and Angular
 * have greatly reduced the significance of the Controller layer on the spring server side.
 *
 * Moreover, over-bloated code structures are not conducive to demonstrating the capabilities
 * of the framework with small examples. Therefore, this example project no longer adheres to
 * dogmatism and directly adds spring web annotations to the service class.
 */
@Api("树结构模型")
@Valid
@Controller
@Mapping("/tree")
public class TreeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TreeService.class);

    @Db
    private TreeNodeRepository treeNodeRepository;


    @ApiOperation("平铺子节点")
    @Get
    @Mapping("/flatNodes")
    public List<FlatTreeNodeView> flatNodes(
            @Param(required = false) String name
    ) {
        return treeNodeRepository.findByNameLikeIgnoreCase(
                name,
                FlatTreeNodeView.class
        );
    }

    @ApiOperation("根据根节点名称查询子节点")
    @Get@Mapping("/roots/recursive")
    public List<@FetchBy("RECURSIVE_FETCHER") TreeNode> findRootTrees( // ❶
            @Param(required = false) String rootName
    ) {
        return treeNodeRepository.findByParentIsNullAndName(
                rootName,
                RECURSIVE_FETCHER
        );
    }

    @ApiOperation("保存树")
    @Put
    @Mapping("/root/recursive")
    @ThrowsAll(SaveErrorCode.class)
    public TreeNode saveTree(@Body RecursiveTreeInput input) { // ❷
        TreeNode rootNode = TreeNodeDraft.$.produce(

                input.toEntity(),

                /*
                 * `TreeNode` has two key properties: `name` and `parent`,
                 * this means `name` and `parent` must be specified when `id` is missing.
                 *
                 * One-to-many association is special, parent object can specify the
                 * many-to-one association of its child objects implicitly.
                 * In this demo, Associations named `childNodes` specify `parent`
                 * for child objects implicitly so that all child objects do not require
                 * the `parent`.
                 *
                 * However, the `parent` of ROOT cannot be specified implicitly,
                 * so that it must be specified manually
                 */
                draft -> draft.setParent(null)
        );
        return treeNodeRepository.save(rootNode);
    }

    @ApiOperation("删除树节点及下级")
    @Delete@Mapping("/{id}")
    public void deleteTree(@Path long id) {
        treeNodeRepository.deleteById(id);
    }

    private static final Fetcher<TreeNode> RECURSIVE_FETCHER =
            TreeNodeFetcher.$
                    .allScalarFields()
                    .childNodes(
                            TreeNodeFetcher.$.allScalarFields(),
                            RecursiveListFieldConfig::recursive
                    );
}

/*----------------Documentation Links----------------
❶ https://babyfish-ct.github.io/jimmer/docs/spring/client/api#declare-fetchby
  https://babyfish-ct.github.io/jimmer/docs/query/object-fetcher/recursive

❷ https://babyfish-ct.github.io/jimmer/docs/mutation/save-command/input-dto/
  https://babyfish-ct.github.io/jimmer/docs/object/view/dto-language#92-recursive-association
---------------------------------------------------*/
