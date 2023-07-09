/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fabricmc.fabric.impl.networking;

import java.util.Objects;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.jetbrains.annotations.Nullable;

/**
 * We still need to support {@link GenericFutureListener} so we use this disguise impl {@link PacketSendListener}
 * to get our {@link GenericFutureListener} to into {@link Connection}.
 */
public final class GenericFutureListenerHolder implements PacketSendListener {
	private final GenericFutureListener<? extends Future<? super Void>> delegate;

	private GenericFutureListenerHolder(GenericFutureListener<? extends Future<? super Void>> delegate) {
		this.delegate = Objects.requireNonNull(delegate);
	}

	@Nullable
	public static GenericFutureListenerHolder create(@Nullable GenericFutureListener<? extends Future<? super Void>> delegate) {
		if (delegate == null) {
			return null;
		}

		return new GenericFutureListenerHolder(delegate);
	}

	public GenericFutureListener<? extends Future<? super Void>> getDelegate() {
		return delegate;
	}

	@Override
	public void onSuccess() {
		throw new AssertionError("Should not be called");
	}

	@Nullable
	@Override
	public Packet<?> onFailure() {
		throw new AssertionError("Should not be called");
	}
}
