package com.neelnetwork.state

import com.neelnetwork.block.Block.BlockId
import com.neelnetwork.consensus.nxt.NxtLikeConsensusBlockData

case class BlockMinerInfo(consensus: NxtLikeConsensusBlockData, timestamp: Long, blockId: BlockId)
