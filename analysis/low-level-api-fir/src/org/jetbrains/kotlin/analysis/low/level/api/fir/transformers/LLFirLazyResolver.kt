/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.low.level.api.fir.transformers

import org.jetbrains.kotlin.analysis.low.level.api.fir.api.targets.LLFirResolveTarget
import org.jetbrains.kotlin.analysis.low.level.api.fir.file.builder.LLFirLockProvider
import org.jetbrains.kotlin.analysis.low.level.api.fir.lazy.resolve.LLFirPhaseUpdater
import org.jetbrains.kotlin.analysis.low.level.api.fir.util.checkPhase
import org.jetbrains.kotlin.analysis.low.level.api.fir.util.forEachDependentDeclaration
import org.jetbrains.kotlin.fir.FirElementWithResolveState
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.resolve.ScopeSession
import org.jetbrains.kotlin.fir.resolve.transformers.body.resolve.FirResolveContextCollector

internal abstract class LLFirLazyResolver(
    val resolverPhase: FirResolvePhase,
) {
    abstract fun resolve(
        target: LLFirResolveTarget,
        lockProvider: LLFirLockProvider,
        session: FirSession,
        scopeSession: ScopeSession,
        towerDataContextCollector: FirResolveContextCollector?,
    )

    fun checkIsResolved(target: FirElementWithResolveState) {
        target.checkPhase(resolverPhase)
        phaseSpecificCheckIsResolved(target)
        checkNestedDeclarationsAreResolved(target)
    }

    /**
     * Check that phase-specific conditions are met
     * Will be performed to resolved declaration and its nested declarations
     * @see checkNestedDeclarationsAreResolved
     */
    protected open fun phaseSpecificCheckIsResolved(target: FirElementWithResolveState) {}

    fun updatePhaseForDeclarationInternals(target: FirElementWithResolveState) {
        LLFirPhaseUpdater.updateDeclarationInternalsPhase(
            target = target,
            newPhase = resolverPhase,
            updateForLocalDeclarations = resolverPhase == FirResolvePhase.BODY_RESOLVE,
        )
    }

    fun checkIsResolved(designation: LLFirResolveTarget) {
        designation.forEachTarget(::checkIsResolved)
    }

    private fun checkNestedDeclarationsAreResolved(target: FirElementWithResolveState) {
        if (target !is FirDeclaration) return

        checkFunctionParametersAreResolved(target)
        checkVariableSubDeclarationsAreResolved(target)
        checkTypeParametersAreResolved(target)
        checkScriptDependentDeclarationsAreResolved(target)
    }

    private fun checkVariableSubDeclarationsAreResolved(declaration: FirDeclaration) {
        if (declaration !is FirVariable) return

        declaration.getter?.let(::checkIsResolved)
        declaration.setter?.let(::checkIsResolved)
        declaration.backingField?.let(::checkIsResolved)
    }

    private fun checkFunctionParametersAreResolved(declaration: FirDeclaration) {
        if (declaration !is FirFunction) return

        for (parameter in declaration.valueParameters) {
            checkIsResolved(parameter)
        }
    }

    private fun checkTypeParametersAreResolved(declaration: FirDeclaration) {
        if (declaration !is FirTypeParameterRefsOwner) return

        for (parameter in declaration.typeParameters) {
            if (parameter !is FirTypeParameter) continue
            checkIsResolved(parameter)
        }
    }

    private fun checkScriptDependentDeclarationsAreResolved(declaration: FirDeclaration) {
        if (declaration !is FirScript) return

        declaration.forEachDependentDeclaration(::checkIsResolved)
    }
}
