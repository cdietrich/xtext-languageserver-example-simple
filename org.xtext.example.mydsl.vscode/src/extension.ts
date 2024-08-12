'use strict';

import * as path from 'path';
import * as os from 'os';
//import * as net from 'net';

import {Trace} from 'vscode-jsonrpc';
import { commands, window, workspace, ExtensionContext, Uri } from 'vscode';
import { LanguageClient, LanguageClientOptions, ServerOptions /*,StreamInfo*/ } from 'vscode-languageclient/node';

let lc: LanguageClient;

export function activate(context: ExtensionContext) {
    // The server is a locally installed in src/mydsl
    const launcher = os.platform() === 'win32' ? 'mydsl-standalone.bat' : 'mydsl-standalone';
    const script = context.asAbsolutePath(path.join('src', 'mydsl', 'bin', launcher));

    /*
    let connectionInfo = {
        port: 5007
    };
    let serverOptions = () => {
        // Connect to language server via socket
        let socket = net.connect(connectionInfo);
        let result: StreamInfo = {
            writer: socket,
            reader: socket
        };
        return Promise.resolve(result);
    };
    */

    const serverOptions: ServerOptions = {
        run : { command: script },
        debug: { command: script, args: [], options: { env: createDebugEnv() } }
    };
    
    const clientOptions: LanguageClientOptions = {
        documentSelector: ['mydsl'],
        synchronize: {
            fileEvents: workspace.createFileSystemWatcher('**/*.*')
        }
    };
    
    // Create the language client and start the client.
    const lc = new LanguageClient('Xtext Server', serverOptions, clientOptions);
    
    const disposable2 =commands.registerCommand("mydsl.a.proxy", async () => {
        const activeEditor = window.activeTextEditor;
        if (!activeEditor || !activeEditor.document || activeEditor.document.languageId !== 'mydsl') {
            return;
        }

        if (activeEditor.document.uri instanceof Uri) {
            commands.executeCommand("mydsl.a", activeEditor.document.uri.toString());
        }
    })
    context.subscriptions.push(disposable2);
    
    // enable tracing (.Off, .Messages, Verbose)
    lc.setTrace(Trace.Verbose);
    lc.start();
}

export function deactivate() {
	return lc.stop();
}

function createDebugEnv() {
    return Object.assign({
        JAVA_OPTS:"-Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=8000,suspend=n,quiet=y"
    }, process.env)
}