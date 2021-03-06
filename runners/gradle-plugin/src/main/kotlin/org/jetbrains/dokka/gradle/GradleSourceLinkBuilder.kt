package org.jetbrains.dokka.gradle

import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.jetbrains.dokka.DokkaConfigurationBuilder
import org.jetbrains.dokka.SourceLinkDefinitionImpl
import java.io.File
import java.net.URL

class GradleSourceLinkBuilder(
    @Transient @get:Internal internal val project: Project
) : DokkaConfigurationBuilder<SourceLinkDefinitionImpl> {

    @Input
    val localDirectory: Property<File?> = project.objects.safeProperty()

    @Input
    val remoteUrl: Property<URL?> = project.objects.safeProperty<URL>()

    @Optional
    @Input
    val remoteLineSuffix: Property<String> = project.objects.safeProperty<String>()
        .safeConvention("#L")

    override fun build(): SourceLinkDefinitionImpl {
        return SourceLinkDefinitionImpl(
            localDirectory = localDirectory.getSafe()?.absolutePath ?: project.projectDir.absolutePath,
            remoteUrl = checkNotNull(remoteUrl.getSafe()) { "missing remoteUrl on source link" },
            remoteLineSuffix = remoteLineSuffix.getSafe()
        )
    }
}
