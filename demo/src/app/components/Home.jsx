import React from 'react';
import FormStore from '../stores/FormStore';
import SubmissionStore from '../stores/SubmissionStore';
import FormActionCreators from '../actions/FormActionCreators';
import SchemaForm from 'react-schema-form/lib/SchemaForm';
import RaisedButton from 'material-ui/lib/raised-button';
import WebAPIUtils from '../utils/WebAPIUtils';

/*
    recent news post    | Permaculture -> Permaculture Nursery
    recent blog post    | Edible Forest Garden -> Dundalk
    recent forum post   | ...

    footer
 */



let Form = React.createClass({

    displayName: 'Form',

    getInitialState: function() {
        return {
            schema: null,
            form: null,
            action: null,
            model: this.props.model
        };
    },

    componentWillMount: function() {
        FormStore.addChangeListener(this._onFormChange);
        SubmissionStore.addChangeListener(this._onSubmissionChange);
        FormActionCreators.getForm(this.props.params.formId);
    },

    componentWillUnmount: function() {
        FormStore.removeChangeListener(this._onFormChange);
        SubmissionStore.removeChangeListener(this._onSubmissionChange);
    },

    _onSubmissionChange: function() {
        // TODO display error or success with toaster.
    },


    _onFormChange: function() {
        let schema = FormStore.getForm(this.props.params.formId) ? FormStore.getForm(this.props.params.formId).schema : null;
        if(schema) {
            let form = FormStore.getForm(this.props.params.formId).form;
            let action = FormStore.getForm(this.props.params.formId).action;
            //console.log('schema = ', schema);
            //console.log('form = ', form);
            //console.log('action = ', action);
            this.setState({
                schema: schema,
                form: form,
                action: action
            });
        }
    },

    _onModelChange: function(key, val) {
        this.setState({model: utils.selectOrSet(key, this.state.model, val)});
    },

    _onTouchTap: function(action) {
        console.log('Form._onTouchTap', action, this.state.model);
        action.data = this.state.model;
        FormActionCreators.submitForm(action);
    },

    render: function() {
        //console.log('Form: props', this.props);
        if(this.state.schema) {
            const buttons = this.state.action.map((item, idx) => (
                <RaisedButton key={idx} label={item.title} primary={true} onTouchTap = {(e) => (WebAPIUtils.submitForm(this.state.action))} />
            ));

            return (
                <div>
                    <SchemaForm schema={this.state.schema} form={this.state.form} model={this.props.model} onModelChange={this._onModelChange} />
                    {buttons}
                </div>
            )
        } else {
            return <div>Loading...</div>
        }
    }
});

module.exports = Form;