package me.myklebust.xpdoctor.validator.nodevalidator.unloadable;

import java.util.Map;

import org.apache.commons.lang.text.StrSubstitutor;

import com.google.common.collect.Maps;

import com.enonic.xp.node.NodeVersionMetadata;

class Substitutor
{
    static String substitute( final String source, final NodeVersionMetadata version )
    {
        final Map<String, String> valueMap = Maps.newHashMap();
        valueMap.put( "name", version.getNodePath().getName() + " [xpDoctor-revived]" );
        valueMap.put( "nodeId", version.getNodeId().toString() );
        valueMap.put( "timestamp", version.getTimestamp().toString() );

        final StrSubstitutor strSubstitutor = new StrSubstitutor( valueMap );

        return strSubstitutor.replace( source );
    }

}
