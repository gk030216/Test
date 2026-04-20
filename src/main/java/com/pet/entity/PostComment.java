package com.pet.entity;

import java.util.Date;
import java.util.List;

public class PostComment {
    private Integer id;
    private Integer postId;
    private Integer userId;
    private Integer parentId;
    private Integer replyToUserId;
    private String replyToUserName;
    private String content;
    private Integer status;
    private Date createTime;

    // 关联字段
    private String userName;
    private String userNickname;
    private String userAvatar;
    private List<PostComment> replies;
    private Boolean isLiked;

    // 原帖相关字段
    private String postTitle;
    private String postContent;
    private Date postCreateTime;
    private String postAuthorName;
    private String postAuthorAvatar;

    // 父评论内容
    private String parentCommentContent;
    // 父评论作者
    private String parentCommentAuthorName;

    // getter/setter（删除 likeCount 相关）
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getPostId() { return postId; }
    public void setPostId(Integer postId) { this.postId = postId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public Integer getParentId() { return parentId; }
    public void setParentId(Integer parentId) { this.parentId = parentId; }

    public Integer getReplyToUserId() { return replyToUserId; }
    public void setReplyToUserId(Integer replyToUserId) { this.replyToUserId = replyToUserId; }

    public String getReplyToUserName() { return replyToUserName; }
    public void setReplyToUserName(String replyToUserName) { this.replyToUserName = replyToUserName; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserAvatar() { return userAvatar; }
    public void setUserAvatar(String userAvatar) { this.userAvatar = userAvatar; }

    public List<PostComment> getReplies() { return replies; }
    public void setReplies(List<PostComment> replies) { this.replies = replies; }

    public Boolean getIsLiked() { return isLiked; }
    public void setIsLiked(Boolean isLiked) { this.isLiked = isLiked; }

    public String getPostTitle() { return postTitle; }
    public void setPostTitle(String postTitle) { this.postTitle = postTitle; }

    public String getPostContent() { return postContent; }
    public void setPostContent(String postContent) { this.postContent = postContent; }

    public Date getPostCreateTime() { return postCreateTime; }
    public void setPostCreateTime(Date postCreateTime) { this.postCreateTime = postCreateTime; }

    public String getPostAuthorName() { return postAuthorName; }
    public void setPostAuthorName(String postAuthorName) { this.postAuthorName = postAuthorName; }

    public String getPostAuthorAvatar() { return postAuthorAvatar; }
    public void setPostAuthorAvatar(String postAuthorAvatar) { this.postAuthorAvatar = postAuthorAvatar; }

    public String getParentCommentContent() { return parentCommentContent; }
    public void setParentCommentContent(String parentCommentContent) { this.parentCommentContent = parentCommentContent; }

    public String getParentCommentAuthorName() { return parentCommentAuthorName; }
    public void setParentCommentAuthorName(String parentCommentAuthorName) { this.parentCommentAuthorName = parentCommentAuthorName; }

    public String getUserNickname() { return userNickname; }
    public void setUserNickname(String userNickname) { this.userNickname = userNickname; }
}