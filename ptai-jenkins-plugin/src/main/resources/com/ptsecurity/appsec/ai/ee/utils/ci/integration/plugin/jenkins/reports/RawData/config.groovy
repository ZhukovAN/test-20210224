package com.ptsecurity.appsec.ai.ee.utils.ci.integration.plugin.jenkins.reports.Report

import lib.FormTagLib

def f = namespace(FormTagLib)

f.entry(
        title: _('fileName'),
        field: 'fileName') {
    f.textbox()
}