import React from 'react';
import classNames from 'classnames';

var CommentNode = React.createClass({

    getInitialState: function() {
        return {comments: [
            {
                "rid": "#36:0",
                "out_HasComment": [
                    {
                        "host": "www.networknt.com",
                        "commentId": "joiwejfloiewwoihg",
                        "createDate": "2016-03-20T17:50:21.455",
                        "comment": "This is the first child of comment 1",
                        "in_": []
                    }
                ],
                "comment": "This is the first comment",
                "commentId": "Hc-X1ag7QziOIffoUQXO5g",
                "createDate": "2016-03-20T14:40:47.317",
                "userId": "stevehu",
                "userRid": "#15:0"
            },
            {
                "rid": "#36:1",
                "comment": "This is the second comment",
                "commentId": "oqzhrj4hTbacItyV9xTYVQ",
                "createDate": "2016-03-20T14:41:05.591",
                "userId": "stevehu",
                "userRid": "#15:0"
            },
            {
                "rid": "#36:2",
                "comment": "This is the third comment",
                "commentId": "GQACmDViS-6w7oZpcPjZfA",
                "createDate": "2016-03-20T14:41:16.028",
                "userId": "stevehu",
                "userRid": "#15:0"
            }
        ]};
    },

    onCategorySelect: function (ev) {
        if (this.props.onCommentSelect) {
            this.props.onCommentSelect(this);
        }
        ev.preventDefault();
        ev.stopPropagation();
    },

    onChildDisplayToggle: function (ev) {
        if (this.props.comment.out_HasComment) {
            if (this.state.out_HasComment && this.state.out_HasComment.length) {
                this.setState({out_HasComment: null});
            } else {
                this.setState({out_HasComment: this.props.comment.out_HasComment});
            }
        }
        ev.preventDefault();
        ev.stopPropagation();
    },

    render: function () {
        if (!this.state.out_HasComment) this.state.out_HasComment = [];
        var classes = classNames({
            'has-children': (this.props.comment.out_HasComment ? true : false),
            'open': (this.state.out_HasComment.length ? true : false),
            'closed': (this.state.out_HasComment ? false : true),
            'selected': (this.state.selected ? true : false)
        });
        return (
            <li ref="node" className={classes}
                onClick={this.onChildDisplayToggle}>
                <a onClick={this.onCommentSelect}
                   data-id={this.props.comment.commentId}>
                    {this.props.comment.commentId}
                </a>
                <ul>
                    {this.state.out_HasComment.map(function(child) {
                        return <CommentNode key={child.commentId}
                                         comment={child}
                                         onCommentSelect={this.props.onCommentSelect}/>;
                    }.bind(this))}
                </ul>
            </li>
        );
    }
});

module.exports = CommentNode;