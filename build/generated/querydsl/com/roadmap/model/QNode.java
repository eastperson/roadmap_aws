package com.roadmap.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QNode is a Querydsl query type for Node
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QNode extends EntityPathBase<Node> {

    private static final long serialVersionUID = 1237431878L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QNode node = new QNode("node");

    public final QBaseEntity _super = new QBaseEntity(this);

    public final ListPath<Node, QNode> childs = this.<Node, QNode>createList("childs", Node.class, QNode.class, PathInits.DIRECT2);

    public final BooleanPath complete = createBoolean("complete");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modDate = _super.modDate;

    public final StringPath nodeType = createString("nodeType");

    public final QNode parent;

    public final BooleanPath read = createBoolean("read");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> regDate = _super.regDate;

    public final StringPath shortDescription = createString("shortDescription");

    public final QStage stage;

    public final StringPath text = createString("text");

    public final StringPath title = createString("title");

    public final StringPath url = createString("url");

    public QNode(String variable) {
        this(Node.class, forVariable(variable), INITS);
    }

    public QNode(Path<? extends Node> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QNode(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QNode(PathMetadata metadata, PathInits inits) {
        this(Node.class, metadata, inits);
    }

    public QNode(Class<? extends Node> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.parent = inits.isInitialized("parent") ? new QNode(forProperty("parent"), inits.get("parent")) : null;
        this.stage = inits.isInitialized("stage") ? new QStage(forProperty("stage"), inits.get("stage")) : null;
    }

}

