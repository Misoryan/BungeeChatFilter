package com.minecraftdimensions.bungeechatfilter;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.chat.BaseComponent;

public class Rule {

    List<String> equal;
    Pattern contain;
    Pattern ignore;
    HashMap<String, Object> actions;
    String permission = null;
    String rule;
    boolean needsPerm;

    public Rule( String name, List<String> equals,String contains, HashMap<String, Object> actions, String permission, String ignores ) {
        this.rule = name;
        this.equal = equals;
        this.contain = Pattern.compile( contains , Pattern.UNICODE_CHARACTER_CLASS);
        if ( ignores == null ) {
            ignore = null;
        }    else{
        this.ignore = Pattern.compile( ignores , Pattern.UNICODE_CHARACTER_CLASS);
        }
        this.actions = actions;
        if(permission != null && permission.startsWith("!")) {
            permission = permission.substring( 1,permission.length() );
            needsPerm = true;
        }
        this.permission = permission;
    }

    public String getName() {return this.rule; }
    public Pattern getContains() {
        return contain;
    }

    public String getStringContains() {
        return contain.pattern();
    }

    public boolean doesMessageContainRegex( String message ) {
        if (contain.pattern().length() != 0) {
            if (contain.matcher(message).find()) {
                return true;
            }
        }
        for (String i : equal) {
            if (message.equalsIgnoreCase(i)) {
                return true;
            }
        }
        return false;
    }

    public void performActions( ChatEvent event, ProxiedPlayer player ) {
        String message = event.getMessage();
        /*
        if(ignore!=null){
            Matcher ig =Pattern.compile(ignore.pattern()).matcher(message);
                while(ig.find()){
                return;
            }
        }
        */
        for ( String action : actions.keySet() ) {
            if ( action.equals( "deny" ) && (Boolean) actions.get( action ) ) {
                event.setCancelled( true );
            } else if ( action.equals( "message" ) ) {
                player.sendMessage( util.MessageFromStringList( (List<String>) actions.get( action ), event ) );
            } else if ( action.equals( "kick" ) ) {
                player.disconnect( util.MessageFromStringList( (List<String>) actions.get( action ), event ) );
            } else if ( action.equals( "alert" ) ) {
                ProxyServer.getInstance().broadcast( util.MessageFromStringList( (List<String>) actions.get( action ), event ) );
            } else if ( action.equals( "scommand" ) ) {
                String command = "";
                for ( String scommand : (List<String>) actions.get( action ) ) {
                    if (command.equals("")) {
                        command = scommand;
                    }
                    else {
                        command = command + ";" + scommand;
                    }
                }
                ByteArrayOutputStream b = new ByteArrayOutputStream();
                DataOutputStream out = new DataOutputStream(b);
                try {
                    out.writeUTF("ExecuteCommand");
                    out.writeUTF(player.getName());
                    out.writeUTF(util.ParseVariables(command,event));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                player.getServer().sendData("BungeeChatFilter", b.toByteArray());
            } else if ( action.equals( "pcommand" ) ) {
                for ( String pcommand : (List<String>) actions.get( action ) ) {
                    ProxyServer.getInstance().getPluginManager().dispatchCommand(player, util.ParseVariables(pcommand, event) );
                }
            } else if( action.equals( "ccommand" ) ) {
                for ( String ccommand : (List<String>) actions.get( action ) ) {
                    ProxyServer.getInstance().getPluginManager().dispatchCommand( ProxyServer.getInstance().getConsole(), util.ParseVariables(ccommand, event) );
                }
            } else if ( action.equals( "remove" ) && (Boolean) actions.get( action ) ) {
                message = message.replaceAll( contain.pattern(), "" );
            } else if ( action.equals( "replace" ) ) {
                Random rand = new Random();
                Matcher m = contain.matcher(message);
                StringBuilder sb = new StringBuilder();
                int last = 0;
                String[] replacements = (String[]) actions.get( action );
                if ( replacements.length > 0 ){
                    while ( m.find() ) {
                            int n = rand.nextInt( replacements.length );
                            sb.append( message.substring( last, m.start() ) );
                            sb.append( util.ParseVariables(((String[]) actions.get( action ))[n], event) );
                            last = m.end();
                    }
                }
                sb.append( message.substring( last ) );
                message = sb.toString();
            } else if ( action.equals( "lower" ) && (Boolean) actions.get( action ) ) {
                Matcher m = contain.matcher(message);
                StringBuilder sb = new StringBuilder();
                int last = 0;
                while ( m.find() ) {
                        sb.append( message.substring( last, m.start() ) );
                        sb.append( m.group( 0 ).toLowerCase() );
                        last = m.end();
                }
                sb.append( message.substring( last ) );
                message = sb.toString();
            }
        }
        event.setMessage( message );
    }

    public boolean hasPermission() {
        return permission != null;
    }

    public boolean needsPermission(){
        return needsPerm;
    }

    public String getPermission() {
        return permission;
    }
}
