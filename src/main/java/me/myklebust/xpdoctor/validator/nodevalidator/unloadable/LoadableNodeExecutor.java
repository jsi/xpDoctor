package me.myklebust.xpdoctor.validator.nodevalidator.unloadable;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import me.myklebust.xpdoctor.validator.RepairResult;
import me.myklebust.xpdoctor.validator.ValidatorResult;
import me.myklebust.xpdoctor.validator.ValidatorResultImpl;
import me.myklebust.xpdoctor.validator.ValidatorResults;
import me.myklebust.xpdoctor.validator.nodevalidator.AbstractNodeExecutor;
import me.myklebust.xpdoctor.validator.nodevalidator.BatchedQueryExecutor;

import com.enonic.xp.node.NodeId;
import com.enonic.xp.node.NodeIds;
import com.enonic.xp.node.NodeService;
import com.enonic.xp.task.ProgressReporter;

public class LoadableNodeExecutor
    extends AbstractNodeExecutor
{
    private final static String TYPE = "Unloadable node";

    public static final int BATCH_SIZE = 1_000;

    private final NodeService nodeService;

    private final LoadableNodeDoctor doctor;

    private Logger LOG = LoggerFactory.getLogger( LoadableNodeExecutor.class );

    public LoadableNodeExecutor( final NodeService nodeService, final ProgressReporter reporter )
    {
        super( reporter );
        this.nodeService = nodeService;
        this.doctor = new LoadableNodeDoctor( this.nodeService );
    }

    public ValidatorResults execute()
    {
        LOG.info( "Running LoadableNodeExecutor..." );

        reportStart();

        final BatchedQueryExecutor executor = BatchedQueryExecutor.create().
            batchSize( BATCH_SIZE ).
            nodeService( this.nodeService ).
            build();

        final ValidatorResults.Builder results = ValidatorResults.create();

        int execute = 0;

        while ( !executor.hasMore() )
        {
            LOG.info( "Checking nodes " + execute + "->" + ( execute + BATCH_SIZE ) + " of " + executor.getTotalHits() );
            reportProgress( executor.getTotalHits(), execute );

            final NodeIds nodesToCheck = executor.execute();
            results.add( checkNodes( nodesToCheck, false ) );
            execute += BATCH_SIZE;
        }

        LOG.info( ".... LoadableNodeExecutor done" );

        return results.build();
    }

    private List<ValidatorResult> checkNodes( final NodeIds nodeIds, final boolean repair )
    {
        List<ValidatorResult> results = Lists.newArrayList();

        for ( final NodeId nodeId : nodeIds )
        {
            doCheckNode( repair, results, nodeId );
        }
        return results;
    }

    private void doCheckNode( final boolean repair, final List<ValidatorResult> results, final NodeId nodeId )
    {
        try
        {
            this.nodeService.getById( nodeId );
        }
        catch ( Exception e )
        {
            final RepairResult repairResult;
            try
            {
                repairResult = this.doctor.repaidNode( nodeId, repair );

                final ValidatorResultImpl result = ValidatorResultImpl.create().
                    nodeId( nodeId ).
                    nodePath( null ).
                    nodeVersionId( null ).
                    timestamp( null ).
                    type( TYPE ).
                    message( e.getMessage() ).
                    repairResult( repairResult ).
                    build();

                results.add( result );
            }
            catch ( Exception e1 )
            {
                LOG.error( "Failed to repair", e1 );
            }
        }
    }
}
