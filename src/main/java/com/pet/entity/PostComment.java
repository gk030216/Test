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
    private Integer likeCount;
    private Integer status;
    private Date createTime;

    // 关联字段
    private String userName;
    private String userAvatar;
    private List<PostComment> replies;
    private Boolean isLiked;

    // 新增：原帖相关字段
    private String postTitle;           // 帖子标题
    private String postContent;         // 帖子内容
    private Date postCreateTime;        // 帖子创建时间
    private String postAuthorName;      // 帖子作者名
    private String postAuthorAvatar;    // 帖子作者头像

    // getter/setter
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

    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }

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

    // 新增字段的 getter/setter
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

}