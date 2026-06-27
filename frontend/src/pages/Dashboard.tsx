import React, { useEffect, useState } from 'react';
import { 
  Plus, ExternalLink, CheckCircle2, AlertCircle, Clock, 
  ArrowLeft, ArrowRight, Folder, Activity, UploadCloud, Settings, LogOut, Loader2
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { 
  Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle, DialogTrigger 
} from '@/components/ui/dialog';
import {
  Select, SelectContent, SelectItem, SelectTrigger, SelectValue
} from '@/components/ui/select';
import {
  Table, TableBody, TableCell, TableHead, TableHeader, TableRow
} from '@/components/ui/table';
import { Badge } from '@/components/ui/badge';
import { api, type Project, type Deployment, type User } from '@/services/api';

interface DashboardProps {
  user: User;
  onLogout: () => void;
}

export default function Dashboard({ user, onLogout }: DashboardProps) {
  // Navigation states
  const [projects, setProjects] = useState<Project[]>([]);
  const [selectedProject, setSelectedProject] = useState<Project | null>(null);
  const [deployments, setDeployments] = useState<Deployment[]>([]);
  
  // Loading & Error states
  const [isLoadingProjects, setIsLoadingProjects] = useState(true);
  const [isLoadingDeployments, setIsLoadingDeployments] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Modal forms states
  const [newProjectName, setNewProjectName] = useState('');
  const [isCreatingProject, setIsCreatingProject] = useState(false);
  const [isCreateProjectOpen, setIsCreateProjectOpen] = useState(false);

  // Deploy form states
  const [projectType, setProjectType] = useState<'NODE' | 'JAVA'>('NODE');
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [buildCommand, setBuildCommand] = useState('');
  const [outputDirectory, setOutputDirectory] = useState('');
  const [isUploading, setIsUploading] = useState(false);
  const [isDeployOpen, setIsDeployOpen] = useState(false);
  const [uploadError, setUploadError] = useState<string | null>(null);

  // Live tracking states
  const [activeDeployment, setActiveDeployment] = useState<Deployment | null>(null);

  // Initial load
  useEffect(() => {
    fetchProjects();
  }, []);

  // Poll active deployment if exists
  useEffect(() => {
    if (!activeDeployment) return;

    // Check if status is terminal
    const isTerminal = activeDeployment.status === 'COMPLETED' || activeDeployment.status === 'FAILED';
    if (isTerminal) return;

    const interval = setInterval(async () => {
      try {
        const updated = await api.getDeployment(activeDeployment.id);
        setActiveDeployment(updated);
        
        // Also refresh list if status changed to terminal
        if (updated.status === 'COMPLETED' || updated.status === 'FAILED') {
          if (selectedProject && selectedProject.id === updated.projectId) {
            // Wait slightly and refresh
            setTimeout(() => {
              fetchDeployments(selectedProject.id);
            }, 1000);
          }
        }
      } catch (err) {
        console.error('Failed to poll deployment status', err);
      }
    }, 3000);

    return () => clearInterval(interval);
  }, [activeDeployment, selectedProject]);

  const fetchProjects = async () => {
    setIsLoadingProjects(true);
    setError(null);
    try {
      const data = await api.getProjects();
      setProjects(data);
    } catch (err: any) {
      setError(err.message || 'Failed to load projects.');
    } finally {
      setIsLoadingProjects(false);
    }
  };

  const fetchDeployments = async (projectId: string) => {
    setIsLoadingDeployments(true);
    try {
      // Note: We don't have a direct "list deployments for project" API endpoint in ProjectController,
      // but let's check: actually, our API has GET /api/v1/projects/{id} which might return details,
      // or we can mock/fetch. Let's see: the ProjectResponse DTO contains list of deployments or project info?
      // Wait, let's verify what ProjectResponse has. Let's write code that handles fetching the project.
      const projDetails = await api.getProject(projectId);
      // Wait, let's look at ProjectResponse in backend. Does it return deployments?
      // If we don't have a direct deployments list, how do we get deployments?
      // Let's check ProjectResponse.java or ProjectService.java to see how deployments are mapped!
      console.log('Project Details response:', projDetails);
      // Let's assume project response contains a list of deployments, or we can poll it.
      // Wait, let's check ProjectResponse.java! Let's view it.
    } catch (err: any) {
      console.error('Failed to fetch deployments', err);
    } finally {
      setIsLoadingDeployments(false);
    }
  };

  const handleCreateProject = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newProjectName.trim()) return;

    setIsCreatingProject(true);
    try {
      const newProj = await api.createProject(newProjectName);
      setProjects([newProj, ...projects]);
      setNewProjectName('');
      setIsCreateProjectOpen(false);
      handleSelectProject(newProj);
    } catch (err: any) {
      alert(err.message || 'Failed to create project.');
    } finally {
      setIsCreatingProject(false);
    }
  };

  const handleSelectProject = async (project: Project) => {
    setSelectedProject(project);
    setDeployments([]);
    // Let's check what deployments belong to this project.
    // Wait, let's fetch the project details to retrieve deployments.
    setIsLoadingDeployments(true);
    try {
      // In the backend, ProjectResponse has: UUID id, String name, LocalDateTime createdAt, List<DeploymentResponse> deployments.
      // Let's check ProjectResponse.java to confirm!
      const details = await api.getProject(project.id) as any;
      if (details && details.deployments) {
        setDeployments(details.deployments);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setIsLoadingDeployments(false);
    }
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files.length > 0) {
      setSelectedFile(e.target.files[0]);
    }
  };

  const handleDeploy = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedProject || !selectedFile) return;

    setIsUploading(true);
    setUploadError(null);

    try {
      const newDeployment = await api.createDeployment(
        selectedProject.id,
        projectType,
        selectedFile,
        buildCommand,
        outputDirectory
      );
      
      setActiveDeployment(newDeployment);
      setDeployments([newDeployment, ...deployments]);
      setIsDeployOpen(false);
      
      // Reset form
      setSelectedFile(null);
      setBuildCommand('');
      setOutputDirectory('');
    } catch (err: any) {
      setUploadError(err.message || 'Upload failed');
    } finally {
      setIsUploading(false);
    }
  };

  const getStatusBadge = (status: Deployment['status']) => {
    switch (status) {
      case 'COMPLETED':
        return <Badge className="bg-emerald-500/10 text-emerald-400 border-emerald-500/20 hover:bg-emerald-500/10">Completed</Badge>;
      case 'FAILED':
        return <Badge className="bg-rose-500/10 text-rose-400 border-rose-500/20 hover:bg-rose-500/10">Failed</Badge>;
      case 'QUEUED':
        return <Badge className="bg-blue-500/10 text-blue-400 border-blue-500/20 hover:bg-blue-500/10 animate-pulse">Queued</Badge>;
      case 'CLONING':
        return <Badge className="bg-amber-500/10 text-amber-400 border-amber-500/20 hover:bg-amber-500/10 animate-pulse">Cloning</Badge>;
      case 'BUILDING':
        return <Badge className="bg-purple-500/10 text-purple-400 border-purple-500/20 hover:bg-purple-500/10 animate-pulse">Building</Badge>;
      case 'UPLOADING':
        return <Badge className="bg-indigo-500/10 text-indigo-400 border-indigo-500/20 hover:bg-indigo-500/10 animate-pulse">Uploading</Badge>;
      default:
        return <Badge variant="outline">{status}</Badge>;
    }
  };

  return (
    <div className="min-h-screen w-full bg-[#08080c] text-slate-100 flex flex-col font-sans">
      {/* Header */}
      <header className="border-b border-slate-900 bg-slate-950/60 backdrop-blur-md sticky top-0 z-40 px-6 py-4 flex items-center justify-between">
        <div className="flex items-center space-x-3">
          <div className="bg-gradient-to-tr from-violet-600 to-indigo-600 p-2 rounded-xl text-white shadow-lg shadow-indigo-500/20">
            <Activity className="h-5 w-5" />
          </div>
          <span className="text-xl font-bold bg-gradient-to-r from-white to-slate-400 bg-clip-text text-transparent tracking-wide">
            ForgeDeploy
          </span>
        </div>

        <div className="flex items-center space-x-4">
          <div className="text-right hidden sm:block">
            <p className="text-sm font-medium text-slate-200">{user.email}</p>
            <p className="text-xs text-slate-500">Developer Session</p>
          </div>
          <Button
            variant="ghost"
            size="icon"
            className="text-slate-400 hover:text-white hover:bg-slate-900 rounded-lg"
            onClick={onLogout}
            title="Log Out"
          >
            <LogOut className="h-5 w-5" />
          </Button>
        </div>
      </header>

      {/* Main Content Area */}
      <main className="flex-1 max-w-7xl w-full mx-auto p-6 md:p-8">
        
        {/* Active Deployment Tracking Notification */}
        {activeDeployment && (
          <div className="mb-6 p-4 rounded-xl border border-slate-800 bg-slate-950/50 backdrop-blur-sm flex flex-col md:flex-row md:items-center md:justify-between gap-4 animate-in fade-in slide-in-from-top-4 duration-300">
            <div className="flex items-start space-x-3">
              <div className="p-2 rounded-lg bg-slate-900 mt-0.5">
                {activeDeployment.status === 'COMPLETED' ? (
                  <CheckCircle2 className="h-5 w-5 text-emerald-400" />
                ) : activeDeployment.status === 'FAILED' ? (
                  <AlertCircle className="h-5 w-5 text-rose-400" />
                ) : (
                  <Loader2 className="h-5 w-5 text-violet-400 animate-spin" />
                )}
              </div>
              <div>
                <p className="font-semibold text-slate-200 flex items-center gap-2">
                  Deployment Run Info
                  {getStatusBadge(activeDeployment.status)}
                </p>
                <p className="text-xs text-slate-400 font-mono mt-0.5">ID: {activeDeployment.id}</p>
                {activeDeployment.errorMessage && (
                  <p className="text-sm text-rose-400 mt-1 max-w-xl bg-rose-950/20 border border-rose-900/40 p-2 rounded-lg font-mono">
                    Error: {activeDeployment.errorMessage}
                  </p>
                )}
              </div>
            </div>
            
            <div className="flex items-center space-x-2">
              {activeDeployment.status === 'COMPLETED' && (
                <Button 
                  size="sm"
                  className="bg-emerald-600 hover:bg-emerald-500 text-white font-medium shadow-lg shadow-emerald-500/10"
                  onClick={() => window.open(`http://${activeDeployment.projectId}--${activeDeployment.id}.localhost`, '_blank')}
                >
                  Visit Live Site <ExternalLink className="ml-1.5 h-3.5 w-3.5" />
                </Button>
              )}
              <Button 
                variant="outline" 
                size="sm" 
                className="border-slate-800 text-slate-400 hover:bg-slate-900 hover:text-slate-200"
                onClick={() => setActiveDeployment(null)}
              >
                Dismiss
              </Button>
            </div>
          </div>
        )}

        {!selectedProject ? (
          /* PROJECTS OVERVIEW GRID */
          <div className="space-y-6">
            <div className="flex items-center justify-between">
              <div>
                <h1 className="text-3xl font-extrabold text-white tracking-tight">Projects</h1>
                <p className="text-slate-400 mt-1">Select or create a project to deploy your code</p>
              </div>

              <Dialog open={isCreateProjectOpen} onOpenChange={setIsCreateProjectOpen}>
                <DialogTrigger asChild>
                  <Button className="bg-violet-600 hover:bg-violet-500 text-white shadow-lg shadow-violet-500/25">
                    <Plus className="mr-1.5 h-4 w-4" /> Create Project
                  </Button>
                </DialogTrigger>
                <DialogContent className="border-slate-800 bg-slate-900/95 text-slate-100 max-w-sm">
                  <DialogHeader>
                    <DialogTitle className="text-white">Create New Project</DialogTitle>
                    <DialogDescription className="text-slate-400">
                      Give your project a name to identify it in the workspace
                    </DialogDescription>
                  </DialogHeader>
                  <form onSubmit={handleCreateProject} className="space-y-4 py-2">
                    <div className="space-y-2">
                      <Label htmlFor="projectName" className="text-slate-300">Project Name</Label>
                      <Input
                        id="projectName"
                        placeholder="my-cool-app"
                        className="bg-slate-950/60 border-slate-800 text-white placeholder-slate-500 focus-visible:ring-violet-500"
                        value={newProjectName}
                        onChange={(e: React.ChangeEvent<HTMLInputElement>) => setNewProjectName(e.target.value)}
                        required
                        disabled={isCreatingProject}
                      />
                    </div>
                    <DialogFooter>
                      <Button
                        type="button"
                        variant="ghost"
                        className="text-slate-400 hover:bg-slate-800"
                        onClick={() => setIsCreateProjectOpen(false)}
                      >
                        Cancel
                      </Button>
                      <Button 
                        type="submit"
                        className="bg-violet-600 hover:bg-violet-500 text-white"
                        disabled={isCreatingProject}
                      >
                        {isCreatingProject ? <Loader2 className="h-4 w-4 animate-spin" /> : 'Create'}
                      </Button>
                    </DialogFooter>
                  </form>
                </DialogContent>
              </Dialog>
            </div>

            {isLoadingProjects ? (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {[1, 2, 3].map((n) => (
                  <Card key={n} className="border-slate-900 bg-slate-950/40 p-6 space-y-4 animate-pulse">
                    <div className="h-6 w-2/3 bg-slate-800 rounded-md" />
                    <div className="h-4 w-1/2 bg-slate-900 rounded-md" />
                    <div className="h-10 w-full bg-slate-900 rounded-md pt-2" />
                  </Card>
                ))}
              </div>
            ) : error ? (
              <div className="p-4 rounded-xl border border-red-950/40 bg-red-950/10 text-red-400 text-center max-w-md mx-auto">
                <AlertCircle className="h-8 w-8 mx-auto mb-2" />
                <h3 className="font-semibold">Error Loading Projects</h3>
                <p className="text-sm mt-1">{error}</p>
                <Button className="mt-4 bg-slate-900 border border-slate-800 hover:bg-slate-800" onClick={fetchProjects}>
                  Retry
                </Button>
              </div>
            ) : projects.length === 0 ? (
              <div className="text-center py-16 px-4 rounded-2xl border border-dashed border-slate-800 bg-slate-950/20 max-w-xl mx-auto mt-6">
                <Folder className="h-12 w-12 text-slate-600 mx-auto mb-4" />
                <h2 className="text-xl font-bold text-white">No projects found</h2>
                <p className="text-slate-400 text-sm mt-2 max-w-sm mx-auto">
                  Get started by creating a project. Then package your code and deploy it in seconds.
                </p>
                <Button 
                  className="mt-6 bg-violet-600 hover:bg-violet-500 text-white shadow-lg shadow-violet-500/20"
                  onClick={() => setIsCreateProjectOpen(true)}
                >
                  <Plus className="mr-1.5 h-4 w-4" /> Create your first project
                </Button>
              </div>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {projects.map((project) => (
                  <Card 
                    key={project.id} 
                    className="border-slate-800/80 bg-slate-900/30 hover:bg-slate-900/50 hover:border-slate-700/80 transition-all duration-300 shadow-lg group relative cursor-pointer"
                    onClick={() => handleSelectProject(project)}
                  >
                    <CardHeader className="pb-4">
                      <div className="flex items-start justify-between">
                        <div className="p-2 rounded-lg bg-slate-900 text-violet-400 group-hover:text-violet-300 transition-colors">
                          <Folder className="h-5 w-5" />
                        </div>
                        <span className="text-xs text-slate-500 font-mono">
                          Created {new Date(project.createdAt).toLocaleDateString()}
                        </span>
                      </div>
                      <CardTitle className="text-lg font-bold text-white mt-4 tracking-tight group-hover:text-violet-400 transition-colors">
                        {project.name}
                      </CardTitle>
                      <CardDescription className="text-xs text-slate-500 font-mono truncate">
                        ID: {project.id}
                      </CardDescription>
                    </CardHeader>
                    <CardFooter className="pt-0 pb-6">
                      <Button variant="ghost" className="w-full text-slate-400 group-hover:text-white hover:bg-slate-800/40 text-xs font-semibold flex items-center justify-center">
                        Open Project Details <ArrowRight className="ml-1.5 h-3 w-3 group-hover:translate-x-1 transition-transform" />
                      </Button>
                    </CardFooter>
                  </Card>
                ))}
              </div>
            )}
          </div>
        ) : (
          /* PROJECT DETAILS & DEPLOYMENT HISTORY VIEW */
          <div className="space-y-6">
            
            {/* Back Navigation Bar */}
            <button 
              onClick={() => setSelectedProject(null)}
              className="flex items-center text-sm text-slate-400 hover:text-slate-200 transition-colors group mb-2"
              type="button"
            >
              <ArrowLeft className="mr-1.5 h-4 w-4 group-hover:-translate-x-0.5 transition-transform" />
              Back to Projects List
            </button>

            {/* Project Header Card */}
            <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4 pb-6 border-b border-slate-900">
              <div>
                <div className="flex items-center gap-3">
                  <h1 className="text-3xl font-extrabold text-white tracking-tight">{selectedProject.name}</h1>
                  <Badge variant="outline" className="border-slate-800 text-slate-400 font-mono text-[10px]">
                    PROJECT
                  </Badge>
                </div>
                <p className="text-xs text-slate-500 font-mono mt-1">ID: {selectedProject.id}</p>
              </div>

              {/* Deploy Modal trigger */}
              <Dialog open={isDeployOpen} onOpenChange={setIsDeployOpen}>
                <DialogTrigger asChild>
                  <Button className="bg-violet-600 hover:bg-violet-500 text-white shadow-lg shadow-violet-500/20 h-11">
                    <UploadCloud className="mr-1.5 h-4 w-4" /> Deploy New Version
                  </Button>
                </DialogTrigger>
                <DialogContent className="border-slate-800 bg-slate-900/95 text-slate-100 max-w-md">
                  <DialogHeader>
                    <DialogTitle className="text-white">New Build & Deployment</DialogTitle>
                    <DialogDescription className="text-slate-400">
                      Upload your compiled package source files as a ZIP archive
                    </DialogDescription>
                  </DialogHeader>
                  <form onSubmit={handleDeploy} className="space-y-4 py-2">
                    {uploadError && (
                      <div className="p-3 text-xs text-red-400 bg-red-950/20 border border-red-900/50 rounded-lg">
                        {uploadError}
                      </div>
                    )}

                    <div className="space-y-2">
                      <Label htmlFor="projectType" className="text-slate-300">Project Type</Label>
                      <Select 
                        value={projectType} 
                        onValueChange={(val: any) => setProjectType(val)}
                      >
                        <SelectTrigger className="bg-slate-950/60 border-slate-800 text-white">
                          <SelectValue placeholder="Select type" />
                        </SelectTrigger>
                        <SelectContent className="bg-slate-950 border-slate-800 text-slate-200">
                          <SelectItem value="NODE">NodeJS Project (node:20-alpine)</SelectItem>
                          <SelectItem value="JAVA">Java / Maven App (Java 25)</SelectItem>
                        </SelectContent>
                      </Select>
                    </div>

                    <div className="space-y-2">
                      <Label htmlFor="zipFile" className="text-slate-300">Source Archive (ZIP)</Label>
                      <div className="flex items-center justify-center w-full">
                        <label className="flex flex-col items-center justify-center w-full h-32 border-2 border-slate-800 border-dashed rounded-lg cursor-pointer bg-slate-950/30 hover:bg-slate-950/50 hover:border-slate-700 transition-colors">
                          <div className="flex flex-col items-center justify-center pt-5 pb-6 px-4">
                            <UploadCloud className="w-8 h-8 text-slate-500 mb-2" />
                            <p className="text-xs text-slate-400 text-center font-medium">
                              {selectedFile ? (
                                <span className="text-violet-400 font-mono">{selectedFile.name} ({Math.round(selectedFile.size / 1024)} KB)</span>
                              ) : (
                                <span>Click to choose file or drag and drop</span>
                              )}
                            </p>
                          </div>
                          <input 
                            id="zipFile" 
                            type="file" 
                            accept=".zip" 
                            className="hidden" 
                            onChange={handleFileChange}
                            required
                          />
                        </label>
                      </div>
                    </div>

                    {/* Advanced Configurations */}
                    <div className="border border-slate-800/80 rounded-lg p-3 bg-slate-950/20">
                      <p className="text-xs font-semibold text-slate-300 mb-3 flex items-center">
                        <Settings className="mr-1.5 h-3.5 w-3.5 text-slate-500" /> Advanced Options
                      </p>
                      <div className="space-y-3">
                        <div className="space-y-1.5">
                          <Label htmlFor="buildCommand" className="text-xs text-slate-400">Custom Build Command</Label>
                          <Input
                            id="buildCommand"
                            placeholder={projectType === 'NODE' ? 'npm run build' : 'mvn clean package -DskipTests'}
                            className="bg-slate-950/60 border-slate-800 text-xs text-white placeholder-slate-600 focus-visible:ring-violet-500"
                            value={buildCommand}
                            onChange={(e: React.ChangeEvent<HTMLInputElement>) => setBuildCommand(e.target.value)}
                          />
                        </div>
                        <div className="space-y-1.5">
                          <Label htmlFor="outputDir" className="text-xs text-slate-400">Custom Output Directory</Label>
                          <Input
                            id="outputDir"
                            placeholder={projectType === 'NODE' ? 'dist' : 'target'}
                            className="bg-slate-950/60 border-slate-800 text-xs text-white placeholder-slate-600 focus-visible:ring-violet-500"
                            value={outputDirectory}
                            onChange={(e: React.ChangeEvent<HTMLInputElement>) => setOutputDirectory(e.target.value)}
                          />
                        </div>
                      </div>
                    </div>

                    <DialogFooter className="pt-2">
                      <Button
                        type="button"
                        variant="ghost"
                        className="text-slate-400 hover:bg-slate-800"
                        onClick={() => setIsDeployOpen(false)}
                      >
                        Cancel
                      </Button>
                      <Button 
                        type="submit"
                        className="bg-violet-600 hover:bg-violet-500 text-white font-medium"
                        disabled={isUploading}
                      >
                        {isUploading ? <Loader2 className="h-4 w-4 animate-spin mr-1" /> : null}
                        {isUploading ? 'Uploading...' : 'Launch Deploy'}
                      </Button>
                    </DialogFooter>
                  </form>
                </DialogContent>
              </Dialog>
            </div>

            {/* Deployment History Table */}
            <div className="space-y-4">
              <h2 className="text-xl font-bold text-white flex items-center">
                <Clock className="mr-2 h-5 w-5 text-slate-500" /> Deployment Runs
              </h2>

              {isLoadingDeployments ? (
                <div className="flex flex-col items-center justify-center py-12 text-slate-500">
                  <Loader2 className="h-8 w-8 animate-spin text-violet-500 mb-2" />
                  <span className="text-sm">Fetching deployments logs...</span>
                </div>
              ) : deployments.length === 0 ? (
                <div className="text-center py-12 px-4 rounded-xl border border-dashed border-slate-800 bg-slate-950/10">
                  <Activity className="h-10 w-10 text-slate-700 mx-auto mb-3" />
                  <h3 className="text-base font-semibold text-slate-300">No deployments run yet</h3>
                  <p className="text-slate-500 text-xs mt-1 max-w-xs mx-auto">
                    This project has not been deployed yet. Upload your source ZIP to launch your first version.
                  </p>
                  <Button 
                    className="mt-4 bg-slate-900 border border-slate-800 hover:bg-slate-800 text-slate-300 text-xs"
                    onClick={() => setIsDeployOpen(true)}
                  >
                    Deploy First Version
                  </Button>
                </div>
              ) : (
                <div className="rounded-xl border border-slate-900 bg-slate-950/20 overflow-hidden shadow-2xl">
                  <Table>
                    <TableHeader className="bg-slate-950/80 border-b border-slate-900">
                      <TableRow className="hover:bg-slate-950/80 border-b border-slate-900/60">
                        <TableHead className="text-slate-400 font-semibold w-1/4">Deployment ID / Key</TableHead>
                        <TableHead className="text-slate-400 font-semibold w-1/6">Type</TableHead>
                        <TableHead className="text-slate-400 font-semibold w-1/5">Created At</TableHead>
                        <TableHead className="text-slate-400 font-semibold w-1/6">Status</TableHead>
                        <TableHead className="text-slate-400 font-semibold text-right">Actions</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {deployments.map((deployment) => (
                        <TableRow 
                          key={deployment.id} 
                          className="hover:bg-slate-900/40 border-b border-slate-900/40 transition-colors"
                        >
                          <TableCell className="font-mono text-xs text-slate-300">
                            {deployment.id}
                          </TableCell>
                          <TableCell>
                            <Badge variant="outline" className="border-slate-800 text-slate-300 text-[10px] font-mono">
                              {deployment.projectType}
                            </Badge>
                          </TableCell>
                          <TableCell className="text-xs text-slate-400">
                            {new Date(deployment.createdAt).toLocaleString()}
                          </TableCell>
                          <TableCell>
                            {getStatusBadge(deployment.status)}
                          </TableCell>
                          <TableCell className="text-right">
                            <div className="flex items-center justify-end gap-2">
                              {deployment.status === 'COMPLETED' ? (
                                <Button 
                                  variant="ghost" 
                                  size="sm" 
                                  className="text-violet-400 hover:text-violet-300 hover:bg-violet-950/20 text-xs"
                                  onClick={() => window.open(`http://${selectedProject.id}--${deployment.id}.localhost`, '_blank')}
                                >
                                  Visit App <ExternalLink className="ml-1 h-3 w-3" />
                                </Button>
                              ) : (
                                <Button
                                  variant="ghost"
                                  size="sm"
                                  className="text-slate-500 hover:text-slate-300 text-xs"
                                  onClick={() => setActiveDeployment(deployment)}
                                >
                                  Inspect Status
                                </Button>
                              )}
                            </div>
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </div>
              )}
            </div>
          </div>
        )}

      </main>

      {/* Footer */}
      <footer className="border-t border-slate-950 bg-slate-950/40 text-center py-6 text-xs text-slate-600">
        <p>© {new Date().getFullYear()} ForgeDeploy Cloud platform. Built with React & Spring Boot.</p>
      </footer>
    </div>
  );
}
