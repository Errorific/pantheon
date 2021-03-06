/*
 * Copyright 2018 ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package tech.pegasys.pantheon.ethereum.jsonrpc.internal.methods.miner;

import tech.pegasys.pantheon.ethereum.blockcreation.AbstractBlockCreator;
import tech.pegasys.pantheon.ethereum.blockcreation.AbstractMiningCoordinator;
import tech.pegasys.pantheon.ethereum.blockcreation.BlockMiner;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.JsonRpcRequest;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.methods.JsonRpcMethod;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcResponse;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcSuccessResponse;

public class MinerStop<C, M extends BlockMiner<C, ? extends AbstractBlockCreator<C>>>
    implements JsonRpcMethod {

  private final AbstractMiningCoordinator<C, M> miningCoordinator;

  public MinerStop(final AbstractMiningCoordinator<C, M> miningCoordinator) {
    this.miningCoordinator = miningCoordinator;
  }

  @Override
  public String getName() {
    return "miner_stop";
  }

  @Override
  public JsonRpcResponse response(final JsonRpcRequest req) {
    if (miningCoordinator != null) {
      miningCoordinator.disable();
    }

    return new JsonRpcSuccessResponse(req.getId(), true);
  }
}
