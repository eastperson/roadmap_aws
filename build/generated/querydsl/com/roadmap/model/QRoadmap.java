package com.roadmap.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QRoadmap is a Querydsl query type for Roadmap
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QRoadmap extends EntityPathBase<Roadmap> {

    private static final long serialVersionUID = -118869128L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QRoadmap roadmap = new QRoadmap("roadmap");

    public final QBaseEntity _super = new QBaseEntity(this);

    public final BooleanPath closed = createBoolean("closed");

    public final DateTimePath<java.time.LocalDateTime> closedDateTime = createDateTime("closedDateTime", java.time.LocalDateTime.class);

    public final BooleanPath complete = createBoolean("complete");

    public final StringPath fullDescription = createString("fullDescription");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath image = createString("image");

    public final SetPath<Member, QMember> likeMembers = this.<Member, QMember>createSet("likeMembers", Member.class, QMember.class, PathInits.DIRECT2);

    public final SetPath<Member, QMember> members = this.<Member, QMember>createSet("members", Member.class, QMember.class, PathInits.DIRECT2);

    public final DateTimePath<java.time.LocalDateTime> milestone = createDateTime("milestone", java.time.LocalDateTime.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modDate = _super.modDate;

    public final QMember owner;

    public final StringPath path = createString("path");

    public final NumberPath<Double> progress = createNumber("progress", Double.class);

    public final BooleanPath published = createBoolean("published");

    public final DateTimePath<java.time.LocalDateTime> publishedDateTime = createDateTime("publishedDateTime", java.time.LocalDateTime.class);

    public final BooleanPath recruiting = createBoolean("recruiting");

    public final DateTimePath<java.time.LocalDateTime> recruitingUpdatedDateTime = createDateTime("recruitingUpdatedDateTime", java.time.LocalDateTime.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> regDate = _super.regDate;

    public final SetPath<Review, QReview> reviews = this.<Review, QReview>createSet("reviews", Review.class, QReview.class, PathInits.DIRECT2);

    public final StringPath shortDescription = createString("shortDescription");

    public final ListPath<Stage, QStage> stageList = this.<Stage, QStage>createList("stageList", Stage.class, QStage.class, PathInits.DIRECT2);

    public final SetPath<Tag, QTag> tags = this.<Tag, QTag>createSet("tags", Tag.class, QTag.class, PathInits.DIRECT2);

    public final StringPath title = createString("title");

    public final BooleanPath useBanner = createBoolean("useBanner");

    public QRoadmap(String variable) {
        this(Roadmap.class, forVariable(variable), INITS);
    }

    public QRoadmap(Path<? extends Roadmap> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QRoadmap(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QRoadmap(PathMetadata metadata, PathInits inits) {
        this(Roadmap.class, metadata, inits);
    }

    public QRoadmap(Class<? extends Roadmap> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.owner = inits.isInitialized("owner") ? new QMember(forProperty("owner"), inits.get("owner")) : null;
    }

}

