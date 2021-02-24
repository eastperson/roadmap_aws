package com.roadmap.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QStage is a Querydsl query type for Stage
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QStage extends EntityPathBase<Stage> {

    private static final long serialVersionUID = -289553606L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QStage stage = new QStage("stage");

    public final QBaseEntity _super = new QBaseEntity(this);

    public final BooleanPath complete = createBoolean("complete");

    public final BooleanPath head = createBoolean("head");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modDate = _super.modDate;

    public final ListPath<Node, QNode> nodeList = this.<Node, QNode>createList("nodeList", Node.class, QNode.class, PathInits.DIRECT2);

    public final NumberPath<Integer> ord = createNumber("ord", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> regDate = _super.regDate;

    public final QRoadmap roadmap;

    public final BooleanPath tail = createBoolean("tail");

    public final StringPath title = createString("title");

    public QStage(String variable) {
        this(Stage.class, forVariable(variable), INITS);
    }

    public QStage(Path<? extends Stage> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QStage(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QStage(PathMetadata metadata, PathInits inits) {
        this(Stage.class, metadata, inits);
    }

    public QStage(Class<? extends Stage> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.roadmap = inits.isInitialized("roadmap") ? new QRoadmap(forProperty("roadmap"), inits.get("roadmap")) : null;
    }

}

