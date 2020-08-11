package org.jetbrains.dokka.base.resolvers.local

import org.jetbrains.dokka.DokkaConfiguration.DokkaSourceSet
import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.resolvers.anchors.SymbolAnchorHint
import org.jetbrains.dokka.base.resolvers.external.ExternalLocationProvider
import org.jetbrains.dokka.base.resolvers.shared.ExternalDocumentationInfo
import org.jetbrains.dokka.base.resolvers.shared.PackageList
import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.model.withDescendants
import org.jetbrains.dokka.pages.*
import org.jetbrains.dokka.plugability.DokkaContext
import org.jetbrains.dokka.plugability.plugin
import org.jetbrains.dokka.plugability.query
import java.util.*

abstract class DefaultLocationProvider(
    protected val pageGraphRoot: RootPageNode,
    protected val dokkaContext: DokkaContext,
    protected val extension: String
) : LocationProvider {
    protected val externalLocationProviderFactories =
        dokkaContext.plugin<DokkaBase>().query { externalLocationProviderFactory }

    protected val packagesIndex: Map<String, ExternalLocationProvider?> = dokkaContext
        .configuration
        .sourceSets
        .flatMap { sourceSet ->
            sourceSet.externalDocumentationLinks.map {
                PackageList.load(it.packageListUrl, sourceSet.jdkVersion, dokkaContext)
                    ?.let { packageList -> ExternalDocumentationInfo(it.url, packageList) }
            }
        }
        .filterNotNull()
        .flatMap { extDocInfo ->
            extDocInfo.packageList.packages.map { packageName ->
                val externalLocationProvider = (externalLocationProviderFactories.asSequence()
                    .mapNotNull { it.getExternalLocationProvider(extDocInfo) }.firstOrNull()
                    ?: run { dokkaContext.logger.error("No ExternalLocationProvider for '${extDocInfo.packageList.url}' found"); null })
                run { null }
                packageName to externalLocationProvider
            }
        }
        .toMap()
        .filterKeys(String::isNotBlank)

    protected open fun getExternalLocation(dri: DRI, sourceSets: Set<DokkaSourceSet>): String? =
        packagesIndex[dri.packageName]?.resolve(dri)

}